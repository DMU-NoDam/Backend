package NoDam.Demo.place.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.*;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
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
        return placeRepository.findAllByGoogleId(googleIds);
    }

    // todo : 장소 시간 고려할 것
    public List<Place> recommendPlaces(
        PlaceType placeType, // not null
        Region region, // not null
        PriceType priceType, // can null
        SeasonType recommendSeason, // can null
        TripThemeType recommendTripTheme, // can null
        WeatherType recommendWeatherType, // can null

        int count
    ) {
        return placeRepository.findPlacesByFilters(
                placeType,
                region.getId(),
                priceType,
                recommendSeason,
                recommendTripTheme,
                recommendWeatherType,
                PageRequest.of(0, count)
        );
    }

}
