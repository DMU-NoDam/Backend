package NoDam.Demo.place.dto;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.common.type.SeasonType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.type.WeatherType;
import NoDam.Demo.place.domain.Place;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfo {

    private Long id;
    private Long regionId;
    private PlaceType placeType;
    private String googleId;
    private String name;
    private String address;
    private Double x;
    private Double y;
    private WeatherType recommendWeatherType;
    private TripThemeType recommendTripThemeType;
    private SeasonType recommendSeasonType;
    private PriceType priceType;

    public static PlaceInfo of(Place place) {
        return PlaceInfo.builder()
                .id(place.getId())
                .regionId(place.getRegionId())
                .placeType(place.getPlaceType())
                .googleId(place.getGoogleId())
                .name(place.getName())
                .address(place.getAddress())
                .x(place.getX())
                .y(place.getY())
                .recommendWeatherType(place.getRecommendWeatherType())
                .recommendTripThemeType(place.getRecommendTripThemeType())
                .recommendSeasonType(place.getRecommendSeasonType())
                .priceType(place.getPriceType())
                .build();
    }

    public Place toPlace() {
        return Place.builder()
                .regionId(regionId)
                .placeType(placeType)
                .googleId(googleId)
                .name(name)
                .address(address)
                .x(x)
                .y(y)
                .recommendWeatherType(recommendWeatherType)
                .recommendTripThemeType(recommendTripThemeType)
                .recommendSeasonType(recommendSeasonType)
                .priceType(priceType)
                .build();
    }

}
