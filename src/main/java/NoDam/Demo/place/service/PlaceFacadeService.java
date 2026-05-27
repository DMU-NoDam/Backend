package NoDam.Demo.place.service;

import NoDam.Demo.ai.AiService;
import NoDam.Demo.ai.Prompt;
import NoDam.Demo.common.type.*;
import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.place.dto.RecommendPlaceRequestDto;
import NoDam.Demo.place.dto.RecommendedPlaceInfo;
import NoDam.Demo.place.dto.google.GooglePlaceInfo;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.dto.ai.AiRecommendPlaceRequestDto;
import NoDam.Demo.plan.dto.ai.AiRecommendPlaceResponseDto;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.plan.service.PlanSelectService;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final GoogleApiService googleApiService;
    private final MapApiService mapApiService;
    private final RegionQueryService regionQueryService;
    private final PlanSelectService planSelectService;
    private final TripSelectService tripSelectService;
    private final AiService aiService;

    private final boolean isMockAi;

    public Place findByGoogleId(String googleId) {
        if(googleId == null || googleId.isEmpty())
            throw new RuntimeException("PlaceFacadeService.findByGoogleId :: parameter googleId can not be null");

        Place place = placeSelectService.findByGoogleId(googleId).orElseGet(
                ()->{return saveNewPlaces(List.of(googleId)).get(0);}
        );

        return place;
    }

    @Async
    public CompletableFuture<List<Place>> findAllByGoogleId(List<String> googleIds) {
        if(googleIds == null || googleIds.isEmpty())
            return CompletableFuture.completedFuture(List.of());

        if(googleIds.contains(null))
            throw new RuntimeException("PlaceFacadeService.findAllByGoogle :: parameter googleIds contain null");

        // DB에서 조회 후 요청 순서대로 정렬 (누락된 장소는 null로 표시됨)
        List<Place> selectedPlaces = ListUtil.sortByRequestOrder(googleIds, placeSelectService.findAllByGoogleId(googleIds), Place::getGoogleId);

        if(selectedPlaces.contains(null)) {
            // DB에 저장되지 않은 place가 존재함 -> missing id 추출
            List<String> notSavedGooglePlaceIds = IntStream.range(0, googleIds.size())
                    .filter(i -> selectedPlaces.get(i) == null)
                    .mapToObj(googleIds::get)
                    .toList();

            List<Place> savedPlaces = saveNewPlaces(notSavedGooglePlaceIds);
            
            // null 제거 후 새로 저장된 장소 추가
            selectedPlaces.removeIf(java.util.Objects::isNull);
            selectedPlaces.addAll(savedPlaces);
        }

        return CompletableFuture.completedFuture(ListUtil.sortByRequestOrder(googleIds, selectedPlaces, Place::getGoogleId));
    }

    private List<Place> saveNewPlaces(List<String> notSavedGoogleIds) {
        List<GooglePlaceInfo> newGooglePlaces = notSavedGoogleIds
                .stream()
                .map(googleId->googleApiService.searchByGoogleId(googleId))
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

    public List<RecommendedPlaceInfo> recommendPlace(RecommendPlaceRequestDto dto, Long userId, WeatherType weather) {
        PlacePlan targetPlan = planSelectService.findPlacePlanWithDatePlanAndTransport(dto.getPlacePlanId());
        DatePlan datePlan = targetPlan.getDatePlan();
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);

        PlaceType placeType = dto.getPlaceType() != null
                ? dto.getPlaceType()
                : placeSelectService.findById(targetPlan.getPlaceId()).getPlaceType();

        Region region = regionQueryService.findById(datePlan.getRegionId());
        List<Place> placedPlaces = planSelectService.findPlacedPlaces(trip, datePlan.getTripThemeType());

        List<PlacePlan> allPlans = planSelectService.findPlacePlansByDatePlan(datePlan)
                .stream()
                .sorted(Comparator.comparing(PlacePlan::getStartTime))
                .toList();

        int targetIndex = IntStream.range(0, allPlans.size())
                .filter(i -> allPlans.get(i).getId().equals(targetPlan.getId()))
                .findFirst()
                .orElse(-1);

        PlaceInfo previousPlace = targetIndex > 0
                ? PlaceInfo.of(placeSelectService.findById(allPlans.get(targetIndex - 1).getPlaceId()))
                : null;
        PlaceInfo nextPlace = targetIndex < allPlans.size() - 1
                ? PlaceInfo.of(placeSelectService.findById(allPlans.get(targetIndex + 1).getPlaceId()))
                : null;

        return recommend(
                placeType, region,
                trip.getPriceType(),
                SeasonType.SPRING, // todo : Season 처리 로직
                datePlan.getTripThemeType(),
                weather,
                trip.getScheduleType(),
                placedPlaces,
                dto.getUserLat(), dto.getUserLon(),
                targetPlan.getStartTime(), targetPlan.getEndTime(),
                previousPlace, nextPlace
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
            RouteInfo route = mapApiService.computeRoutesNavitimeFromCoord(userLat, userLon, result.getPlace(), startTime);
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

        AiRecommendPlaceResponseDto aiResponse = aiService.call(
                Prompt.RECOMMEND_PLACE,
                AiRecommendPlaceResponseDto.class,
                aiRequest
        );

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
