package NoDam.Demo.place.service;

import NoDam.Demo.common.type.*;
import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.place.dto.RecommendPlaceRequestDto;
import NoDam.Demo.place.dto.google.GooglePlaceInfo;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlaceFacadeService {

    private final PlaceSelectService placeSelectService;
    private final PlaceQueryService placeQueryService;
    private final GoogleApiService googleApiService;
    private final RegionQueryService regionQueryService;
    private final PlanSelectService planSelectService;
    private final TripSelectService tripSelectService;

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

    public List<PlaceInfo> recommendPlace(RecommendPlaceRequestDto dto, Long userId, WeatherType weather) {
        PlacePlan targetPlan = planSelectService.findPlacePlanWithDatePlanAndTransport(dto.getPlacePlanId());
        DatePlan datePlan = targetPlan.getDatePlan();
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);

        PlaceType placeType = dto.getPlaceType() != null
                ? dto.getPlaceType()
                : placeSelectService.findById(targetPlan.getPlaceId()).getPlaceType();

        Region region = regionQueryService.findById(datePlan.getRegionId());

        List<Long> excludeIds = planSelectService.findPlacePlansByDatePlan(datePlan)
                .stream()
                .map(PlacePlan::getPlaceId)
                .toList();

        return recommend(
                placeType, region,
                trip.getPriceType(),
                SeasonType.SPRING, // todo : Season 처리 로직
                datePlan.getTripThemeType(),
                weather,
                trip.getScheduleType(),
                excludeIds,
                dto.getUserLat(), dto.getUserLon()
        );
    }

    private List<PlaceInfo> recommend(
            PlaceType placeType,
            Region region,
            PriceType priceType,
            SeasonType seasonType,
            TripThemeType themeType,
            WeatherType weather,
            ScheduleType scheduleType,
            List<Long> excludeIds,
            Double userLat,
            Double userLon
    ) {
        // 1차 필터: 조건 맞는 장소 10개
        List<PlaceInfo> candidates = placeSelectService
                .recommendPlaces(placeType, region, priceType, seasonType, themeType, weather, excludeIds, 10)
                .stream()
                .map(PlaceInfo::of)
                .toList();

        // transport 계산 + 못가는 장소 제거
        List<Pair<PlaceInfo, RouteInfo>> reachable = new ArrayList<>();
        for (PlaceInfo place : candidates) {
            RouteInfo route = googleApiService.computeRouteSummary(userLat, userLon, place.getLat(), place.getLon());
            if (route != null) {
                reachable.add(Pair.of(place, route));
            }
        }

        // 2차 AI: 상위 5개 선정
        return selectTopFiveWithAi(reachable, scheduleType, themeType);
    }

    private List<PlaceInfo> selectTopFiveWithAi(
            List<Pair<PlaceInfo, RouteInfo>> candidates,
            ScheduleType scheduleType,
            TripThemeType themeType
    ) {
        // todo : ai 연동 (현재는 단순 상위 5개)
        return candidates.stream()
                .limit(5)
                .map(Pair::getFirst)
                .toList();
    }

}
