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
    private PriceType priceType;
    private Double lon; // x
    private Double lat; // y

    public static PlaceInfo of(Place place) {
        return PlaceInfo.builder()
                .id(place.getId())
                .regionId(place.getRegionId())
                .placeType(place.getPlaceType())
                .googleId(place.getGoogleId())
                .name(place.getName())
                .address(place.getAddress())
                .lon(place.getLon())
                .lat(place.getLat())
                .priceType(place.getPriceType())
                .build();
    }

    public static PlaceInfo empty() {
        return PlaceInfo.builder()
                .name("empty place")
                .build();
    }

    public Place toPlace() {
        return Place.builder()
                .regionId(regionId)
                .placeType(placeType)
                .googleId(googleId)
                .name(name)
                .address(address)
                .lon(lon)
                .lat(lat)
                .priceType(priceType)
                .build();
    }

}
