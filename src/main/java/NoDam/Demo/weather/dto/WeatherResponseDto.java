package NoDam.Demo.weather.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WeatherResponseDto {
    private String cityName;
    private List<DailyWeather> forecast;
    private CurrentWeather current; // 지금 날씨 (예보 조회일 때만, 기후 정보면 null)
    private String message;

    @Getter
    @Builder
    public static class CurrentWeather {
        private Double temperature;
        private Integer weatherCode;
    }

    @Getter
    @Builder
    public static class DailyWeather {
        private LocalDate date;
        private Double maxTemperature;
        private Double minTemperature;
        private Integer weatherCode;
        private String sourceType; // "FORECAST" or "CLIMATE"
    }
}
