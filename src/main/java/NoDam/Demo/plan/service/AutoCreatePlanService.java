package NoDam.Demo.plan.service;

import NoDam.Demo.adapter.google.GooglePort;
import NoDam.Demo.adapter.google.dto.GooglePlaceInfo;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.*;
import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.place.service.AirportSelectService;
import NoDam.Demo.adapter.route.RoutePort;
import NoDam.Demo.place.service.PlaceQueryService;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.plan.domain.AirportSchedule;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.TransportLeg;
import NoDam.Demo.plan.dto.request.DatePlanRequestDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.stay.service.XoteloSearchService;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripRequest;
import NoDam.Demo.trip.service.TripLockService;
import NoDam.Demo.trip.service.TripRequestService;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AutoCreatePlanService {

    private final PlanCreateService planCreateService;
    private final PlanSelectService planSelectService;
    private final PlaceSelectService placeSelectService;
    private final RegionQueryService regionQueryService;
    private final RoutePort routePort;
    private final AssignService assignService;
    private final AirportSelectService airportSelectService;
    private final DayScheduleService dayScheduleService;
    private final XoteloSearchService hotelService;
    private final GooglePort googlePort;
    private final PlaceQueryService placeQueryService;
    private final TransportPlanService transportPlanService;
    private final TripSelectService tripSelectService;
    private final TripRequestService tripRequestService;
    private final TripLockService tripLockService;

    private final Logger logger = LoggerFactory.getLogger(AutoCreatePlanService.class);

    // 2. google/공항code -> db place 변환 후 TripRequest에 저장
    // 동시성 : getGooglePlaceListOrSave 의 select-then-save 는 place.googleId unique 제약으로 정합성 보장
    @Async
    public CompletableFuture<TripRequest> translateGooglePlaceToDbPlace(Long tripId, Long userId) {
        tripSelectService.findById(tripId, userId); // 소유권 검증

        // 1. TripRequest가 변환(google 조회)이 필요한 google id 목록을 스스로 판단 (필수 장소 + 호텔)
        List<String> googleIds = tripRequestService.findGoogleIdsToConvert(tripId);

        // 2. google id -> place 변환 (없으면 저장), google id 기준 map 구성
        Map<String, Place> placeByGoogleId = getGooglePlaceListOrSave(googleIds).stream()
                .collect(Collectors.toMap(Place::getGoogleId, place -> place, (a, b) -> a));

        // 3. 변환 결과를 TripRequest에 반영 (공항은 code -> place id 정적 매핑)
        TripRequest updated = tripRequestService.updateConvertedPlaces(tripId, placeByGoogleId);

        // 4. 공항 place 존재 검증 (Place 도메인 관심사이므로 facade에서 처리)
        List<Long> airportPlaceIds = new ArrayList<>();
        if (updated.getDepartAirportPlaceId() != null) airportPlaceIds.add(updated.getDepartAirportPlaceId());
        if (updated.getArriveAirportPlaceId() != null) airportPlaceIds.add(updated.getArriveAirportPlaceId());
        if (placeSelectService.findAllById(airportPlaceIds).size() != airportPlaceIds.size())
            throw new CustomException(ErrorCode.NOT_FOUND);

        logger.info("translateGooglePlaceToDbPlace end tripId={}", tripId);
        return CompletableFuture.completedFuture(updated);
    }

    // 3. List<DatePlan> 생성 (2번에서 변환된 place id 사용, google 재호출 없음)
    @Async
    public CompletableFuture<List<DatePlan>> generateAllDatePlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        List<DatePlan> existing = planSelectService.findAllDatePlan(trip);
        if (existing != null && !existing.isEmpty())
            return CompletableFuture.completedFuture(existing); // 멱등성 처리

        TripRequest tripRequest = tripRequestService.findByTripId(tripId);

        // TripRequest 스냅샷 -> 도메인 객체 해석 (2번에서 변환된 place id 로드, google 재호출 없음)
        List<Region> necessaryRegions = regionQueryService.findRegionsByCode(tripRequest.getRegionCodes());
        List<Place> necessaryPlaces = placeSelectService.findAllById(tripRequest.getSelectedPlaceIds());
        Optional<Place> hotel = tripRequest.getHotelPlaceId() != null
                ? Optional.of(placeSelectService.findById(tripRequest.getHotelPlaceId())) : Optional.empty();
        Optional<AirportSchedule> arriveFlight = tripRequest.getArriveAirportPlaceId() != null
                ? Optional.of(new AirportSchedule(
                        placeSelectService.findById(tripRequest.getArriveAirportPlaceId()),
                        tripRequest.getArriveTime() != null ? tripRequest.getArriveTime().toLocalTime() : null))
                : Optional.empty();
        Optional<AirportSchedule> departFlight = tripRequest.getDepartAirportPlaceId() != null
                ? Optional.of(new AirportSchedule(
                        placeSelectService.findById(tripRequest.getDepartAirportPlaceId()),
                        tripRequest.getDepartTime() != null ? tripRequest.getDepartTime().toLocalTime() : null))
                : Optional.empty();

        // 실제 DatePlan 생성은 공통 로직에 위임 (planning lock 포함)
        List<DatePlan> datePlans = autoGenerateDatePlans(
                trip, necessaryRegions, necessaryPlaces, hotel, departFlight, arriveFlight);

        // 3번 완료 -> TripRequest 삭제
        tripRequestService.deleteByTripId(tripId);

        logger.info("generateAllDatePlans end tripId={}", tripId);
        return CompletableFuture.completedFuture(datePlans);
    }

    private List<DatePlan> autoGenerateDatePlans(
            Trip trip, // created trip domain

            List<Region> necessaryRegions,
            List<Place> necessaryPlaces,
            Optional<Place> hotel,

            Optional<AirportSchedule> departFlight,
            Optional<AirportSchedule> arriveFlight
    ) {
        List<DatePlan> tripDates = planSelectService.findAllDatePlan(trip);

        if (tripDates == null || !tripDates.isEmpty())
            return tripDates; // 멱등성 처리

        return tripLockService.runWithLock(trip, () -> {

            List<LocalDate> dates = DateUtil.toDateRange(trip.getStartDate(), trip.getEndDate());

            // 1. Region 배정 (AI or fallback)
            Map<LocalDate, Region> dateRegionMap = assignService.assignRegion(
                    dates,
                    necessaryRegions,
                    necessaryPlaces,
                    arriveFlight.map(AirportSchedule::airport),
                    departFlight.map(AirportSchedule::airport)
            );

            // 2. 공항 배정
            Place recommendArrival = airportSelectService.findAirportByRegion(dateRegionMap.get(trip.getStartDate()));
            Place recommendDepart = airportSelectService.findAirportByRegion(dateRegionMap.get(trip.getEndDate()));
            Map<LocalDate, AirportSchedule> airportByDate = assignService.assignAirport(
                    trip.getStartDate(), trip.getEndDate(),
                    arriveFlight, departFlight,
                    recommendArrival, recommendDepart
            );

            // 3. 호텔 추천 - 사용자 입력 없을 때만 region별 1회 추천 후 Place 변환, 배정은 assignService로
            Map<Long, Place> recommendedHotelByRegion = new HashMap<>();
            if (hotel.isEmpty()) {
                // region당 1회 추천 (외부 api 중복 호출 방지)
                Map<Long, String> hotelGoogleIdByRegion = new HashMap<>();
                for (Region region : dateRegionMap.values())
                    hotelGoogleIdByRegion.computeIfAbsent(region.getId(),
                            id -> hotelService.recommendHotel(region).getPlaceId());
                // google id -> place 일괄 변환
                Map<String, Place> placeByGoogleId = getGooglePlaceListOrSave(new ArrayList<>(hotelGoogleIdByRegion.values()))
                        .stream().collect(Collectors.toMap(Place::getGoogleId, place -> place));
                hotelGoogleIdByRegion.forEach((regionId, googleId) ->
                        recommendedHotelByRegion.put(regionId, placeByGoogleId.get(googleId)));
            }
            Map<LocalDate, Place> hotelByDate = assignService.assignHotel(dates, dateRegionMap, recommendedHotelByRegion, hotel);

            // 4. 필수 장소 날짜별 분배
            Map<LocalDate, List<Place>> necessaryPlacesByDate = assignService.distribute(necessaryPlaces, dates);

            // 5. DatePlan 생성 (TripThemeType별)
            List<DatePlanRequestDto> datePlanRequestDto = dates.stream()
                    .map(date -> {
                        AirportSchedule airport = airportByDate.get(date);
                        return new DatePlanRequestDto(
                                date, dateRegionMap.get(date), trip.getTripThemeType(),
                                necessaryPlacesByDate.get(date), hotelByDate.get(date),
                                airport != null ? airport.airport() : null,
                                airport != null ? airport.time() : null
                        );
                    })
                    .toList();
            List<DatePlan> datePlans = planCreateService.createDatePlans(trip, datePlanRequestDto);

            logger.info("autoGenerateDatePlans end tripId={}", trip.getId());
            return datePlans;
        });
    }

    private List<Place> getGooglePlaceListOrSave(List<String> googleIdList) {
        // 요청 순서대로 조회 (누락된 장소는 null)
        List<Place> selectedPlaces = placeSelectService.findAllByGoogleId(googleIdList);
        if(!selectedPlaces.contains(null))
            return selectedPlaces;

        // 없는 값들만 google port 호출 대상으로 추출
        List<String> missingIds = IntStream.range(0, googleIdList.size())
                .filter(i -> selectedPlaces.get(i) == null)
                .mapToObj(googleIdList::get)
                .toList();

        List<GooglePlaceInfo> googlePlaceInfos = missingIds.stream()
                .map(googleId -> googlePort.searchByGoogleId(googleId))
                .toList();

        List<Region> regionList = googlePlaceInfos.stream()
                .map(placeInfo -> regionQueryService.findByCoordinate(placeInfo.getLat(), placeInfo.getLon()))
                .toList();

        List<PlaceRequestDto> requestDtos = new ArrayList<>();
        for(int i = 0; i < googlePlaceInfos.size(); i++){
            requestDtos.add(googlePlaceInfos.get(i).toPlaceDto(regionList.get(i)));
        }

        List<Place> savedPlaces = placeQueryService.saveAll(requestDtos);

        // 기존 조회 결과(null 제거) + 새로 저장된 장소 합쳐 요청 순서대로 반환
        List<Place> merged = new ArrayList<>(selectedPlaces);
        merged.removeIf(Objects::isNull);
        merged.addAll(savedPlaces);
        return ListUtil.sortByRequestOrder(googleIdList, merged, Place::getGoogleId);
    }

    @Async
    public CompletableFuture<List<DatePlan>> autoGenerateAllPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripLockService.runWithLock(trip, () -> {
            List<DatePlan> createdDatePlans = new ArrayList<>();

            for (DatePlan datePlan : planSelectService.findAllDatePlan(trip)) {
                PlanStatus status = datePlan.getPlanStatus();

                // 1. 공항, 호텔 생성
                if (status.isBefore(PlanStatus.FIXED_PLANNED)) {
                    planCreateService.createFixedPlans(trip, datePlan);
                }

                // 2. 후보 장소 조회 (HOTEL, AIRPORT 제외)
                if (status.isBefore(PlanStatus.AI_PLANNED)) {
                    List<Place> necessaryPlaces = placeSelectService.findAllById(datePlan.getNecessaryPlaces());
                    Region region = regionQueryService.findById(datePlan.getRegionId());
                    List<Place> excludePlaces = planSelectService.findPlacedPlaces(trip, datePlan.getTripThemeType());
                    Map<PlaceType, List<RecommendPlaceResult>> candidates = placeSelectService.recommendPlacesByType(
                            region, trip.getPriceType(), null, trip.getTripThemeType(), null, excludePlaces, 5
                    );

                    List<PlacePlan> placedPlacePlans = planSelectService.findPlacePlansByDatePlan(datePlan);
                    Map<Long, Place> placeById = placeSelectService.findAllById(ListUtil.map(placedPlacePlans, PlacePlan::getPlaceId))
                            .stream()
                            .collect(Collectors.toMap(Place::getId, Function.identity()));
                    Map<PlacePlan, Place> placeMap = placedPlacePlans.stream()
                            .collect(Collectors.toMap(Function.identity(), placePlan -> placeById.get(placePlan.getPlaceId())));

                    List<PlacePlanInfo> fixedPlans = placedPlacePlans.stream()
                            .map(placePlan -> PlacePlanInfo.of(placePlan, placeMap.get(placePlan)))
                            .toList();

                    // 3. AI 일정 생성
                    List<PlacePlanRequestDto> generatedPlans = dayScheduleService.buildSchedule(
                            trip.getScheduleType(), datePlan.getTripThemeType(),
                            necessaryPlaces, fixedPlans, candidates);

                    // validate ai response (ai가 존재하지 않는 place id값을 반환했는지 확인
                    List<Long> placeIds = generatedPlans.stream().map(PlacePlanRequestDto::getPlaceId).toList();
                    List<Place> places = ListUtil.sortByRequestOrder(
                            generatedPlans, PlacePlanRequestDto::getPlaceId,
                            placeSelectService.findAllById(placeIds), (place) -> place.getId()
                    );
                    if (places.contains(null)) {
                        throw new CustomException(ErrorCode.API_FAIL);
                    }

                    createdDatePlans.add(planCreateService.createPlacePlans(datePlan, generatedPlans));
                }
            }

            logger.info("autoGenerateAllPlans end tripId={}", trip.getId());
            return CompletableFuture.completedFuture(createdDatePlans);
        });
    }

    @Async
    public CompletableFuture<List<TransportPlan>> autoGenerateAllThemeTransportPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripLockService.runWithLock(trip, () -> {
            List<TransportPlan> allCreated = new ArrayList<>();

            for (DatePlan datePlan : planSelectService.findAllDatePlan(trip)) {
                if (datePlan.getPlanStatus().isAfterOrEqual(PlanStatus.TRANSPORT_PLANNED)) continue;
                allCreated.addAll(generateTransportPlansByLeg(datePlan));
            }

            logger.info("autoGenerateAllThemeTransportPlans end tripId={}", trip.getId());
            return CompletableFuture.completedFuture(allCreated);
        });
    }

    private List<TransportPlan> generateTransportPlansByLeg(DatePlan targetDate) {
        List<TransportLeg> legs = transportPlanService.findEmptyTransportLegs(targetDate);
        List<Long> placeIds = legs.stream()
                .flatMap(leg -> List.of(leg.from().getPlaceId(), leg.to().getPlaceId()).stream())
                .distinct()
                .toList();

        Map<Long, Place> placeMap = placeSelectService.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, place -> place));

        Map<TransportLeg, RouteInfo> results = new HashMap<>();
        for (TransportLeg leg : legs) {
            RouteInfo routeInfo = routePort.computeRoutesFromPlace(
                    placeMap.get(leg.from().getPlaceId()),
                    placeMap.get(leg.to().getPlaceId()),
                    leg.from().getEndTime()
            );
            if (routeInfo == null) continue;

            results.put(leg, routeInfo);
        }

        return transportPlanService.saveTransportLegs(targetDate, results);
    }

}
