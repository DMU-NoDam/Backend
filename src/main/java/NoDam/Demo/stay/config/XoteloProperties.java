package NoDam.Demo.stay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "external.xotelo")
public class XoteloProperties {
    private String searchUrl;
    private String ratesUrl;
    private String rapidApiKey;
    private String rapidApiHost;
}
