package NoDam.Demo.flight.dto;

import lombok.Getter;

@Getter
public class AirLabsResponseDto {

    private FlightData response;

    @Getter
    public static class FlightData {
        private String flight_iata;
        private String dep_iata;
        private String arr_iata;
        private String dep_time;
        private String arr_time;
    }
}