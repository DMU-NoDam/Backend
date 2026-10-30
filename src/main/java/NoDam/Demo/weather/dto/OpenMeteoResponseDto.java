package NoDam.Demo.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenMeteoResponseDto {
    private Daily daily;
    private Current current;

    @Getter
    @Setter
    public static class Current {
        @JsonProperty("temperature_2m")
        private Double temperature;
        @JsonProperty("weather_code")
        private Integer weatherCode;
    }

    @Getter
    @Setter
    public static class Daily {
        private List<String> time;
        @JsonProperty("weathercode")
        private List<Integer> weatherCode;
        @JsonProperty("temperature_2m_max")
        private List<Double> maxTemp;
        @JsonProperty("temperature_2m_min")
        private List<Double> minTemp;
    }
}
