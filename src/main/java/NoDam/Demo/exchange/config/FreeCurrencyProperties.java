package NoDam.Demo.exchange.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.freecurrency")
public class FreeCurrencyProperties {

    private String baseUrl;
    private String apiKey; // .env의 FREECURRENCY_API_KEY, 없으면 빈 문자열(환율 조회만 실패)
}
