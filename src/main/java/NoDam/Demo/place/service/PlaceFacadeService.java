package NoDam.Demo.place.service;

import NoDam.Demo.adapter.ai.AiPort;
import NoDam.Demo.adapter.ai.Prompt;
import NoDam.Demo.adapter.google.GooglePort;
import NoDam.Demo.adapter.route.RoutePort;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.*;
import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.place.dto.RecommendPlaceRequestDto;
import NoDam.Demo.place.dto.RecommendedPlaceInfo;
import NoDam.Demo.adapter.google.dto.GooglePlaceInfo;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.dto.ai.AiRecommendPlaceRequestDto;
import NoDam.Demo.plan.dto.ai.AiRecommendPlaceResponseDto;
import NoDam.Demo.plan.dto.ai.DatePlanDto;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.plan.repository.DatePlanDtoPort;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.service.PlanSelectService;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlaceFacadeService {

    private final PlaceSelectService placeSelectService;
    private final PlaceQueryService placeQueryService;
    private final GooglePort googlePort;
    private final RoutePort routePort;
    private final RegionQueryService regionQueryService;
    private final TripSelectService tripSelectService;
    private final AiPort aiPort;
    private final DatePlanDBPort datePlanDBPort;

    private final boolean isMockAi;

    private final Logger logger = LoggerFactory.getLogger(PlaceFacadeService.class);
    private final PlacePlanRepository placePlanRepository;

    public Place findByGoogleId(String googleId) {
        if(googleId == null || googleId.isEmpty())
            throw new RuntimeException("PlaceFacadeService.findByGoogleId :: parameter googleId can not be null");

        Place place = placeSelectService.findByGoogleId(googleId).orElseGet(
                ()->{return saveNewPlaces(List.of(googleId)).get(0);}
        );

        return place;
    }

    private List<Place> saveNewPlaces(List<String> notSavedGoogleIds) {
        List<GooglePlaceInfo> newGooglePlaces = notSavedGoogleIds
                .stream()
                .map(googleId->googlePort.searchByGoogleId(googleId))
                .toList();
        List<PlaceRequestDto> placeRequestDtos = new ArrayList<>();

        List<PlaceRequestDto> requestDtos =  newGooglePlaces
                .stream()
                .map(googleDto ->
                        googleDto.toPlaceDto(regionQueryService.findByCoordinate(googleDto.getLat(), googleDto.getLon()))
                )
                .toList();

        return placeQueryService.saveAll(requestDtos);
    }

    // todo : plan facade랑 코드 곂침
    public List<RecommendedPlaceInfo> recommendPlace(RecommendPlaceRequestDto dto, Long userId, WeatherType weather) {
        Long datePlanId = placePlanRepository.findById(dto.getPlacePlanId())
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND))
                .getDatePlan().getId(); // todo : request dto를 수정할 수 없음, dto에서 date plan id값도 알려줬어야함

        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);
        PlacePlan targetPlacePlan = datePlan.findPlacePlan(dto.getPlacePlanId());

        PlaceType placeType = dto.getPlaceType() != null
                ? dto.getPlaceType()
                : placeSelectService.findById(targetPlacePlan.getPlaceId()).getPlaceType();

        Region region = regionQueryService.findById(datePlan.getRegionId());
        List<Long> placeIds = ListUtil.map(datePlan.getPlacePlans(), PlacePlan::getPlaceId);

        Map<Long, Place> placeMap = new HashMap<>();
        for (Place place : placeSelectService.findAllById(placeIds))
            placeMap.put(place.getId(), place);

        List<Place> placedPlaces = ListUtil.map(placeIds, placeMap::get);

        PlaceInfo previousPlaceInfo = PlaceInfo.of(placeMap.get(datePlan.findPreviousPlacePlan(targetPlacePlan.getId()).getPlaceId()));
        PlaceInfo nextPlaceInfo = PlaceInfo.of(placeMap.get(datePlan.findNextPlacePlan(targetPlacePlan.getId()).getPlaceId()));

        return recommend(
                placeType, region,
                trip.getPriceType(),
                SeasonType.SPRING, // todo : Season 처리 로직
                datePlan.getTripThemeType(),
                weather,
                trip.getScheduleType(),
                placedPlaces,
                dto.getUserLat(), dto.getUserLon(),
                targetPlacePlan.getStartTime(), targetPlacePlan.getEndTime(),
                previousPlaceInfo, nextPlaceInfo
        );
    }

    private List<RecommendedPlaceInfo> recommend(
            PlaceType placeType,
            Region region,
            PriceType priceType,
            SeasonType seasonType,
            TripThemeType themeType,
            WeatherType weather,
            ScheduleType scheduleType,
            List<Place> excludePlaces,
            Double userLat,
            Double userLon,
            LocalTime startTime,
            LocalTime endTime,
            PlaceInfo previousPlace,
            PlaceInfo nextPlace
    ) {
        // 1차 필터: 조건 맞는 장소 10개
        List<RecommendPlaceResult> candidates = placeSelectService
                .recommendPlaces(placeType, region, priceType, seasonType, themeType, weather, excludePlaces, 10);

        // transport 계산 (null이면 RouteInfo.empty로 포함)
        List<Pair<RecommendPlaceResult, RouteInfo>> reachable = new ArrayList<>();
        for (RecommendPlaceResult result : candidates) {
            RouteInfo route = routePort.computeRoutesFromCoord(userLat, userLon, result.getPlace(), startTime);
            reachable.add(Pair.of(result, route != null ? route : RouteInfo.empty()));
        }

        // 2차 AI: 상위 5개 선정
        return selectTopFiveWithAi(reachable, scheduleType, themeType, startTime, endTime, previousPlace, nextPlace);
    }

    private List<RecommendedPlaceInfo> selectTopFiveWithAi(
            List<Pair<RecommendPlaceResult, RouteInfo>> candidates,
            ScheduleType scheduleType,
            TripThemeType themeType,
            LocalTime oldStartTime,
            LocalTime oldEndTime,
            PlaceInfo previousPlace,
            PlaceInfo nextPlace
    ) {
        if(isMockAi)
            return candidates.stream()
                    .limit(5)
                    .map(pair -> RecommendedPlaceInfo.of(pair.getFirst().getPlace(), pair.getSecond(), oldStartTime, oldEndTime))
                    .toList();

        AiRecommendPlaceRequestDto aiRequest = AiRecommendPlaceRequestDto.builder()
                .scheduleType(scheduleType)
                .themeType(themeType)
                .previousPlace(previousPlace)
                .nextPlace(nextPlace)
                .candidates(candidates.stream()
                        .map(pair -> AiRecommendPlaceRequestDto.PlaceCandidate.of(pair.getFirst(), pair.getSecond()))
                        .toList())
                .build();

        AiRecommendPlaceResponseDto aiResponse;
        try {
            aiResponse = aiPort.call(
                    Prompt.RECOMMEND_PLACE,
                    AiRecommendPlaceResponseDto.class,
                    aiRequest
            );
        } catch (CustomException e) {
            if(!e.errorCode.equals(ErrorCode.API_FAIL))
                throw e;

            return candidates.stream()
                    .limit(5)
                    .map(pair -> RecommendedPlaceInfo.of(pair.getFirst().getPlace(), pair.getSecond(), oldStartTime, oldEndTime))
                    .toList();
        }

        Map<Long, Pair<RecommendPlaceResult, RouteInfo>> candidateMap = candidates.stream()
                .collect(Collectors.toMap(pair -> pair.getFirst().getPlace().getId(), pair -> pair));

        return aiResponse.getSelectedPlaces().stream()
                .map(selected -> {
                    Pair<RecommendPlaceResult, RouteInfo> pair = candidateMap.get(selected.getPlaceId());
                    LocalTime startTime = TimeUtil.toLocalTime(selected.getStartTime());
                    LocalTime endTime = TimeUtil.toLocalTime(selected.getEndTime());
                    return RecommendedPlaceInfo.of(
                            pair.getFirst().getPlace(),
                            pair.getSecond(),
                            startTime != null ? startTime : oldStartTime,
                            endTime != null ? endTime : oldEndTime
                    );
                })
                .toList();
    }

}
