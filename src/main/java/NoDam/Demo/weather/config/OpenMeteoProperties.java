package NoDam.Demo.weather.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class OpenMeteoProperties {
    private final String baseUrl = "https://api.open-meteo.com/v1/forecast";
}
