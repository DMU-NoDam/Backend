package NoDam.Demo.weather.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.weather.config.OpenMeteoProperties;
import NoDam.Demo.weather.dto.OpenMeteoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenMeteoService {

    private final WebClient.Builder webClientBuilder;
    private final OpenMeteoProperties properties;

    public OpenMeteoResponseDto getForecast(Double lat, Double lon) {
        log.info("Calling Open-Meteo API for lat: {}, lon: {}", lat, lon);

        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.open-meteo.com")
                        .path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("daily", "weathercode,temperature_2m_max,temperature_2m_min")
                        .queryParam("timezone", "Asia/Tokyo")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> Mono.error(new CustomException(ErrorCode.BAD_REQUEST)))
                .bodyToMono(OpenMeteoResponseDto.class)
                .block();
    }
}
