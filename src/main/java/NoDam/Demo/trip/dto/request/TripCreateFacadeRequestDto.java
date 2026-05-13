package NoDam.Demo.trip.dto.request;

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

    // trip 관련
    @NotNull
    private TripCreateDto trip;

    // trip date 관련
    @NotEmpty
    private List<String> region;

    private List<String> selectedPlace;

    private List<String> hotel;

    // flight 관련
    private FlightInfo departFlight;
    private FlightInfo arriveFlight;

    public static class FlightInfo {

        // todo : convert enum 필요함 (airport code -> region code)
        private String airport; // airport code
        private String time; // yyyy-mm-dd
    }

}
