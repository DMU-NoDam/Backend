package NoDam.Demo.place.dto.google;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.region.domain.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GooglePlaceInfo {

    private String placeId;

    private List<String> googleTypes;

    private String name;
    private String address;

    private Double lon; // x
    private Double lat; // y

    private GooglePriceType priceType;

    public void setPriceType(String priceLevel) {
        if (priceLevel == null) return;
        try {
            this.priceType = GooglePriceType.valueOf(priceLevel);
        } catch (IllegalArgumentException e) {
            this.priceType = GooglePriceType.PRICE_LEVEL_UNSPECIFIED;
        }
    }

    public enum GooglePriceType {

        PRICE_LEVEL_UNSPECIFIED (null),  // → 알 수 없음
        PRICE_LEVEL_FREE        (PriceType.CHEEP),  // → 무료
        PRICE_LEVEL_INEXPENSIVE (PriceType.CHEEP),  // → 저렴
        PRICE_LEVEL_MODERATE    (PriceType.NORMAL),  // → 보통 1~1000 / 1000~2000
        PRICE_LEVEL_EXPENSIVE   (PriceType.LUXURY),  // → 비쌈
        PRICE_LEVEL_VERY_EXPENSIVE (PriceType.LUXURY), // → 매우 비쌈
        ;

        GooglePriceType(PriceType priceType) {
            this.priceType = priceType;
        }
        PriceType priceType;
    }

    private enum GooglePlaceType {
        RESTAURANT (PlaceType.RESTAURANT, "restaurant", "bakery", "bar", "pub"),
        CAFE(PlaceType.CAFE, "cafe", "cafeteria"),
        SIGHT(PlaceType.SIGHT, "art_gallery", "museum", "zoo", "park", "plaza", "garden", "tourist_attraction", "aquarium", "ski_resort", "fishing_pond", "golf_course"),
        SHOP(PlaceType.SHOP, "market", "store", "supermarket", "hypermarket"),
        HOTEL(PlaceType.HOTEL, "bed_and_breakfast", "budget_japanese_inn", "campground", "camping_cabin", "cottage", "farmstay", "guest_house", "hostel", "hotel", "inn", "japanese_inn", "lodging", "motel", "private_guest_room", "resort_hotel"),
        AIRPORT(PlaceType.AIRPORT,"airport"),
        ;

        GooglePlaceType(PlaceType localType, String... googleIncludeStrings) {
            this.placeType = localType;
            this.googleIncludeStrings = Arrays.stream(googleIncludeStrings).map(str->str.toLowerCase()).toList();
        }
        PlaceType placeType;
        List<String> googleIncludeStrings;

        static PlaceType convert(List<String> googleTypes) {
            for(String googleType : googleTypes) {
                for(GooglePlaceType type : GooglePlaceType.values()) {
                    for(String googleIncludeString : type.googleIncludeStrings)
                        if(googleType.toLowerCase().contains(googleIncludeString))
                            return type.placeType;
                }
            }
            return null;
        }
    }

    private static PlaceType toPlaceType(List<String> googleTypes) {
        if (googleTypes == null || googleTypes.isEmpty()) return null;
        for(String googleType : googleTypes) {
            for(PlaceType localType : PlaceType.values()) {
                if(googleType.toLowerCase().contains(localType.name().toLowerCase()))
                    return localType;
            }
        }

        return null;
    }

    public PlaceRequestDto toPlaceDto(Region region) {
        PlaceRequestDto dto = new PlaceRequestDto();
        dto.setRegion(region);
        dto.setPlaceType(toPlaceType(this.googleTypes));
        dto.setGoogleId(this.placeId);
        dto.setName(this.name);
        dto.setAddress(this.address);
        dto.setLon(this.lon);
        dto.setLat(this.lat);
        dto.setWeatherType(null);
        dto.setTripThemeType(null);
        dto.setSeasonType(null);
        dto.setPriceType(priceType != null ? priceType.priceType : null);
        return dto;
    }

}
