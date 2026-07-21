package NoDam.Demo.place.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.*;
import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.domain.TypeWeight;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceSelectService {

    private final PlaceRepository placeRepository;

    public Place findById(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));
    }

    public List<Place> findAllById(List<Long> placeIds) {
        return placeRepository.findAllById(placeIds);
    }

    public Optional<Place> findByGoogleId(String googleId) {
        return placeRepository.findByGoogleId(googleId);
    }

    public List<Place> findAllByGoogleId(List<String> googleIds) {
        List<Place> selectedPlaces = placeRepository.findAllByGoogleId(googleIds);
        return ListUtil.sortByRequestOrder(googleIds, selectedPlaces, (p)->p.getGoogleId());
    }

    // todo : 한번의 query로 처리 고려할 것
    public Map<PlaceType, List<RecommendPlaceResult>> recommendPlacesByType(
            Region region, // not null
            PriceType priceType, // can null
            SeasonType recommendSeason, // can null
            TripThemeType recommendTripTheme, // can null
            WeatherType recommendWeatherType, // can null
            List<Place> excludePlaces, // 제외할 place 목록
            int count
    ) {
        Map<PlaceType, List<RecommendPlaceResult>> result = new HashMap<>();
        for (PlaceType placeType : PlaceType.values()) {
            result.put(placeType, recommendPlaces(
                    placeType, region, priceType, recommendSeason,
                    recommendTripTheme, recommendWeatherType, excludePlaces, count));
        }
        return result;
    }

    // todo : 장소 시간 고려할 것
    public List<RecommendPlaceResult> recommendPlaces(
        PlaceType placeType, // not null
        Region region, // not null
        PriceType priceType, // can null
        SeasonType recommendSeason, // can null
        TripThemeType recommendTripTheme, // can null
        WeatherType recommendWeatherType, // can null
        List<Place> excludePlaces, // 제외할 place 목록
        int count
    ) {
        List<Long> excludeIds = excludePlaces == null || excludePlaces.isEmpty()
                ? List.of(-1L)
                : excludePlaces.stream().map(Place::getId).toList();
        List<Place> places = placeRepository.findPlacesByFilters(
                placeType.name(),
                region.getId(),
                priceType            != null ? priceType.name()            : null,
                recommendSeason      != null ? recommendSeason.name()      : null,
                recommendTripTheme   != null ? recommendTripTheme.name()   : null,
                recommendWeatherType != null ? recommendWeatherType.name() : null,
                excludeIds,
                PageRequest.of(0, count)
        );
        return places.stream()
                .map(place -> new RecommendPlaceResult(
                        PlaceInfo.of(place),
                        TypeWeight.computeDetail(place, priceType, recommendSeason, recommendTripTheme, recommendWeatherType)
                ))
                .toList();
    }

}
