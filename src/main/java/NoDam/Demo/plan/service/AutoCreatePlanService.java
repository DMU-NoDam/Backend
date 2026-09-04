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
import NoDam.Demo.plan.domain.TransportLeg;
import NoDam.Demo.plan.dto.request.DatePlanRequestDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.adapter.hotel.HotelPort;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripRequest;
import NoDam.Demo.trip.service.TripLockService;
import NoDam.Demo.trip.service.TripRequestService;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AutoCreatePlanService {

    private final PlanCreateService planCreateService;
    private final PlaceSelectService placeSelectService;
    private final RegionQueryService regionQueryService;
    private final RoutePort routePort;
    private final AssignService assignService;
    private final AirportSelectService airportSelectService;
    private final DayScheduleService dayScheduleService;
    private final HotelPort hotelPort;
    private final GooglePort googlePort;
    private final PlaceQueryService placeQueryService;
    private final TripSelectService tripSelectService;
    private final TripRequestService tripRequestService;
    private final TripLockService tripLockService;
    private final DatePlanDBPort datePlanDBPort;

    private final Logger logger = LoggerFactory.getLogger(AutoCreatePlanService.class);

    // 2. google/공항code -> db place 변환 후 TripRequest에 저장
    // 동시성 : getGooglePlaceListOrSave 의 select-then-save 는 place.googleId unique 제약으로 정합성 보장
    public void translateGooglePlaceToDbPlace(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId); // 소유권 검증

        // 3번(DatePlan 생성)이 끝났으면 2번도 끝난 것이다.
        // TripRequest는 그 시점에 삭제되므로 재호출 시 여기서 종료한다 (락도 잡지 않는다)
        List<DatePlan> existing = datePlanDBPort.datePlans(tripId);
        if (existing != null && !existing.isEmpty()) return;

        tripLockService.runWithLock(trip, ()-> {
            // transport

            // 1. TripRequest가 변환(google 조회)이 필요한 google id 목록을 스스로 판단 (필수 장소 + 호텔)
            List<String> googleIds = tripRequestService.findGoogleIdsToConvert(tripId);

            // 2. google id -> place 변환 (없으면 저장), google id 기준 map 구성
            Map<String, Place> placeByGoogleId = getGooglePlaceListOrSave(googleIds).stream()
                    .collect(Collectors.toMap(Place::getGoogleId, place -> place, (a, b) -> a));

            // 3. 변환 결과를 TripRequest에 반영 (공항은 code -> place id 정적 매핑)
            TripRequest updated = tripRequestService.updateConvertedPlaces(tripId, placeByGoogleId);

            // 4. 공항 place 존재 검증 (Place 도메인 관심사이므로 facade에서 처리)
            if (updated.getDepartAirportPlaceId() != null)
                placeSelectService.findById(updated.getDepartAirportPlaceId());
            if (updated.getArriveAirportPlaceId() != null)
                placeSelectService.findById(updated.getArriveAirportPlaceId());

            logger.info("translateGooglePlaceToDbPlace end tripId={}", tripId);
            return updated;
        });
    }

    // 3. List<DatePlan> 생성 (2번에서 변환된 place id 사용, google 재호출 없음)
    public List<DatePlan> generateAllDatePlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        List<DatePlan> existing = datePlanDBPort.datePlans(tripId);
        if (existing != null && !existing.isEmpty())
            return existing; // 멱등성 처리

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
        return datePlans;
    }

    private List<DatePlan> autoGenerateDatePlans(
            Trip trip, // created trip domain

            List<Region> necessaryRegions,
            List<Place> necessaryPlaces,
            Optional<Place> hotel,

            Optional<AirportSchedule> departFlight,
            Optional<AirportSchedule> arriveFlight
    ) {
        List<DatePlan> tripDates = datePlanDBPort.datePlans(trip.getId());

        if (tripDates != null && !tripDates.isEmpty())
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
            Map<Region, Place> recommendedHotelByRegion = new HashMap<>();
            if (hotel.isEmpty()) {
                // region당 1회 추천 (외부 api 중복 호출 방지)
                Map<Region, String> hotelGoogleIdByRegion = new HashMap<>();
                for (Region region : new HashSet<>(dateRegionMap.values()))
                    hotelPort.recommendHotelGoogleId(region)
                            .ifPresent(googleId -> hotelGoogleIdByRegion.put(region, googleId));
                // google id -> place 일괄 변환
                Map<String, Place> placeByGoogleId = getGooglePlaceListOrSave(new ArrayList<>(hotelGoogleIdByRegion.values()))
                        .stream().collect(Collectors.toMap(Place::getGoogleId, place -> place));
                hotelGoogleIdByRegion.forEach((region, googleId) ->
                        recommendedHotelByRegion.put(region, placeByGoogleId.get(googleId)));
            }
            Map<LocalDate, Place> hotelByDate = assignService.assignHotel(dates, dateRegionMap, recommendedHotelByRegion, hotel);

            // 4. 필수 장소 날짜별 분배
            Map<LocalDate, List<Place>> necessaryPlacesByDate = assignService.distribute(necessaryPlaces, dates);

            // 5. DatePlan 생성 (TripThemeType별)
            // 지역/공항/호텔/필수장소는 날짜로만 결정되므로 테마별로 동일한 값을 공유한다 (총 날짜수 * 테마수 개 생성)
            List<DatePlanRequestDto> datePlanRequestDto = dates.stream()
                    .flatMap(date -> {
                        AirportSchedule airport = airportByDate.get(date);
                        return Arrays.stream(TripThemeType.values())
                                .map(themeType -> new DatePlanRequestDto(
                                        date, dateRegionMap.get(date), themeType,
                                        necessaryPlacesByDate.get(date), hotelByDate.get(date),
                                        airport != null ? airport.airport() : null,
                                        airport != null ? airport.time() : null
                                ));
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

    public void autoGenerateAllPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        tripLockService.runWithLock(trip, () -> {
            List<DatePlan> datePlans = datePlanDBPort.datePlans(tripId);
            // DatePlan은 날짜 x 테마로 만들어지므로 제외 목록도 테마별로 분리한다 (테마끼리 후보를 뺏지 않도록)
            Map<TripThemeType, List<Place>> excludePlacesByTheme = new HashMap<>();

            // 1. 공항, 호텔 생성
            for (int i = 0; i < datePlans.size(); i++) {
                DatePlan datePlan = datePlans.get(i);
                PlanStatus status = datePlan.getPlanStatus();

                if (status.isBefore(PlanStatus.FIXED_PLANNED)) {
                    datePlan = planCreateService.createFixedPlans(trip, datePlan); // 공항, 호텔이 반영된 domain으로 교체
                    datePlans.set(i, datePlan);
                }

                List<Place> placedPlaces = placeSelectService.findAllById(datePlan.getPlacePlans().stream().map(PlacePlan::getPlaceId).toList());
                excludePlacesByTheme.computeIfAbsent(datePlan.getTripThemeType(), t -> new ArrayList<>()).addAll(placedPlaces);
            }

            // 3. ai 일정 생성
            for (DatePlan datePlan : datePlans) {
                PlanStatus status = datePlan.getPlanStatus();

                // 후보 장소 조회 (HOTEL, AIRPORT 제외)
                if (status.isBefore(PlanStatus.AI_PLANNED)) {
                    List<Place> necessaryPlaces = placeSelectService.findAllById(datePlan.getNecessaryPlaces());
                    Region region = regionQueryService.findById(datePlan.getRegionId());
                    List<Place> excludePlaces = excludePlacesByTheme.computeIfAbsent(datePlan.getTripThemeType(), t -> new ArrayList<>());

                    // 후보 조회
                    Map<PlaceType, List<RecommendPlaceResult>> candidates = placeSelectService.recommendPlacesByType(
                            region, trip.getPriceType(), null, datePlan.getTripThemeType(), null,
                            excludePlaces, List.of(PlaceType.RESTAURANT, PlaceType.CAFE, PlaceType.SIGHT, PlaceType.SHOP), 5
                    );

                    // AI 일정 생성
                    List<PlacePlan> fixedPlacePlans = datePlan.getPlacePlans();
                    Map<Long, Place> placeMap = placeSelectService.findAllById(fixedPlacePlans.stream().map(PlacePlan::getPlaceId).toList())
                            .stream()
                            .collect(Collectors.toMap(Place::getId, Function.identity()));
                    List<PlacePlanInfo> fixedPlans = fixedPlacePlans.stream()
                            .map(placePlan -> PlacePlanInfo.of(placePlan, placeMap.get(placePlan.getPlaceId()), null))
                            .toList();
                    List<PlacePlanRequestDto> generatedPlans = dayScheduleService.buildSchedule(
                            trip.getScheduleType(), datePlan.getTripThemeType(),
                            necessaryPlaces, fixedPlans, candidates);

                    // validate ai response (ai가 존재하지 않는 place id값을 반환했는지 확인)
                    List<Long> placeIds = generatedPlans.stream().map(PlacePlanRequestDto::getPlaceId).toList();
                    List<Place> places = ListUtil.sortByRequestOrder(
                            generatedPlans, PlacePlanRequestDto::getPlaceId,
                            placeSelectService.findAllById(placeIds), (place) -> place.getId()
                    );
                    if (places.contains(null)) {
                        throw new CustomException(ErrorCode.API_FAIL);
                    }

                    planCreateService.createPlacePlans(datePlan, generatedPlans);
                    excludePlaces.addAll(places); // 같은 테마의 뒤 날짜 후보에서 제외한다
                }
            }

            logger.info("autoGenerateAllPlans end tripId={}", trip.getId());
            return null;
        });
    }

    public void autoGenerateAllThemeTransportPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        tripLockService.runWithLock(trip, () -> {

            for (DatePlan datePlan : datePlanDBPort.datePlans(tripId)) {
                if (datePlan.getPlanStatus().isAfterOrEqual(PlanStatus.TRANSPORT_PLANNED)) continue;
                generateTransportPlansByLeg(datePlan);
            }

            logger.info("autoGenerateAllThemeTransportPlans end tripId={}", trip.getId());
            return null;
        });
    }

    // todo : transport async single thread 랑 코드 비슷함
    private void generateTransportPlansByLeg(DatePlan datePlan) {
        List<TransportLeg> legList = datePlan.getDetachedTransportLegs();

        for (TransportLeg leg : legList) {
            Place from = placeSelectService.findById(leg.from().getPlaceId());
            Place to = placeSelectService.findById(leg.to().getPlaceId());

            RouteInfo routeInfo = routePort.computeRoutesFromPlace(from, to, leg.from().getEndTime());
            // route info null이어도 일단 add 처리 --> 건너 뛰기 기능이 없음

            // 저장 가능한지 여부는 domain 내부 판단
            datePlan.addTransportPlan(leg, routeInfo);
        }

        datePlan.updatePlanStatus(PlanStatus.TRANSPORT_PLANNED); // todo : 남은 transport plan이 있다면 fall back 처리 필요
        datePlanDBPort.saveDatePlan(datePlan);
    }

}
