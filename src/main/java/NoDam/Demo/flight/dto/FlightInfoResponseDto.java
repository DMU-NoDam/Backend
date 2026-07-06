package NoDam.Demo.flight.dto;

import NoDam.Demo.flight.service.AirportCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FlightInfoResponseDto {

    private String flightIata;
    private AirportCode departureAirport;
    private AirportCode arrivalAirport;
    private String departureTime;  // "yyyy-MM-dd HH:mm" 형식
    private String arrivalTime;    // "yyyy-MM-dd HH:mm" 형식
    private Integer duration;      // 비행 시간 (분)
}