package NoDam.Demo.flight.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.airlabs")
public class AirLabsProperties {

    private String baseUrl;
    private String apiKey;
}