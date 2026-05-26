package NoDam.Demo.place.dto.google;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleRouteRequestDto {

    private LocationInput origin;
    private LocationInput destination;
    private String travelMode;
    private String languageCode;
    private String departureTime;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationInput {
        private LocationWrapper location;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationWrapper {
        private LatLng latLng;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LatLng {
        private Double latitude;
        private Double longitude;
    }

    public static GoogleRouteRequestDto transit(Double originLat, Double originLng, Double destinationLat, Double destinationLng, LocalTime startTime) {
        GoogleRouteRequestDto dto = new GoogleRouteRequestDto();
        dto.setOrigin(new LocationInput(new LocationWrapper(new LatLng(originLat, originLng))));
        dto.setDestination(new LocationInput(new LocationWrapper(new LatLng(destinationLat, destinationLng))));
        dto.setTravelMode("TRANSIT");
        dto.setLanguageCode("ko");
        ZonedDateTime departure = ZonedDateTime.of(LocalDate.now(), startTime, ZoneId.of("Asia/Tokyo"));
        dto.setDepartureTime(departure.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return dto;
    }
}
