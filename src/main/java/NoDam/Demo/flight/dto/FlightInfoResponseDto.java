package NoDam.Demo.flight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FlightInfoResponseDto {

    private String flightIata;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime;
}