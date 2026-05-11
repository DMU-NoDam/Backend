package NoDam.Demo.flight.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class AirLabsResponseDto {

    // 수정 : /schedules API 는 response 가 배열로 반환됨
    private List<FlightData> response;
    private ErrorData error;

    @Getter
    public static class FlightData {
        private String flight_iata;
        private String dep_iata;
        private String arr_iata;
        private String dep_time;
        private String arr_time;
    }

    @Getter
    public static class ErrorData {
        private String message;
        private String code;

        @Override
        public String toString() {
            return String.format("[%s] %s", code, message);
        }
    }
}