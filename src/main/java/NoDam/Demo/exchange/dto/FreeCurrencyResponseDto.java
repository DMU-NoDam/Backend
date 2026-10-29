package NoDam.Demo.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

// FreeCurrencyAPI /v1/latest 응답 : {"data": {"JPY": 0.1177}}
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FreeCurrencyResponseDto {
    private Map<String, Double> data;
}
