package NoDam.Demo.flight.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirLabsResponseDto {

    // 수정 : /schedules API 는 response 가 배열로 반환됨
    private List<FlightData> response;
    private ErrorData error;

    @Getter
    public static class FlightData {
        private String flight_iata;
        private String dep_iata;
        private String arr_iata;
        private String dep_time;   // "HH:mm" 형식
        private String arr_time;   // "HH:mm" 형식
        private Integer duration;  // 비행 시간 (분)
        private java.util.List<String> days; // 운항 요일 ["mon", "tue", ...]
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