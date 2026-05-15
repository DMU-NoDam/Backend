package NoDam.Demo.place.dto;

import NoDam.Demo.common.type.*;
import NoDam.Demo.region.domain.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlaceRequestDto {

    private Region region;

    private PlaceType placeType;
    private String googleId;

    private String name;

    private String address;

    private Double lon; // x
    private Double lat; // y

    private WeatherType weatherType; // can null
    private TripThemeType tripThemeType; // can null
    private SeasonType seasonType; // can null

    private PriceType priceType; // can null

}
