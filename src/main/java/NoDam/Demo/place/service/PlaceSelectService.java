package NoDam.Demo.place.service;

import NoDam.Demo.adapter.google.GooglePort;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.*;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Coordinate;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.domain.TypeWeight;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.place.dto.google.GooglePlaceInfo;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.stay.dto.XoteloSearchResponseDto;
import NoDam.Demo.stay.service.XoteloSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceSelectService {

    private final PlaceRepository placeRepository;
    private final GooglePort googlePort;

    public Place findById(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));
    }

    public List<Place> findAllById(List<Long> placeIds) {
        return placeRepository.findAllById(placeIds);
    }

    // todo : google port 사용해서 saveNewPlaces까지 책임 지도록
    public Place findByGoogleId(String googleId) {
        return placeRepository.findByGoogleId(googleId).get();
    }

    // todo : google port 사용해서 saveNewPlaces까지 책임 지도록
    public List<Place> findAllByGoogleId(List<String> googleIds) {
        // 없는 값 save new 까지 처리 + sort by request
        return placeRepository.findAllByGoogleId(googleIds);
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
