package NoDam.Demo.trip.dto.request;

import NoDam.Demo.flight.type.AirportCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TripCreateFacadeRequestDto {

    @NotNull
    private TripCreateDto trip;

    @NotEmpty
    private List<String> region;

    private List<String> selectedPlace;

    private FlightInfo departFlight;
    private FlightInfo arriveFlight;
    private String hotel; // 숙소 google id (단일)

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FlightInfo {
        private AirportCode airport;
        private String time; // yyyy-MM-dd HH:mm
    }

}
