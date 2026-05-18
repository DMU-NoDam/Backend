package NoDam.Demo.place.dto.google;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GooglePlaceResponseDto {

    private String id;
    private List<String> types;
    private String formattedAddress;
    private LocationDto location;
    private DisplayNameDto displayName;
    private String priceLevel;
    private Double rating;
    private String googleMapsUri;
    private String websiteUri;
    private ParkingOptionsDto parkingOptions;

    @Getter
    @Setter
    public static class LocationDto {
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    public static class DisplayNameDto {
        private String text;
        private String languageCode;
    }

    @Getter
    @Setter
    public static class ParkingOptionsDto {
        private Boolean freeParkingLot;
    }

    public GooglePlaceInfo toGooglePlaceInfo() {
        GooglePlaceInfo info = new GooglePlaceInfo();
        info.setPlaceId(this.id);
        info.setGoogleTypes(this.types);
        info.setAddress(this.formattedAddress);
        
        if (this.displayName != null) {
            info.setName(this.displayName.getText());
        }
        
        if (this.location != null) {
            info.setLat(this.location.getLatitude());
            info.setLon(this.location.getLongitude());
        }
        
        if (this.priceLevel != null) {
            info.setPriceType(this.priceLevel);
        }
        
        return info;
    }
}
