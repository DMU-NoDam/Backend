package NoDam.Demo.flight.service;

import NoDam.Demo.flight.config.AirLabsProperties;
import NoDam.Demo.flight.dto.AirLabsResponseDto;
import NoDam.Demo.flight.dto.FlightInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AirLabsService {

    private final WebClient.Builder webClientBuilder;
    private final AirLabsProperties properties;

    public FlightInfoResponseDto getFlightInfo(String flightIata) {

        String raw = webClientBuilder.build() //
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("airlabs.co")
                        .path("/api/v9/flight")
                        .queryParam("flight_iata", flightIata)
                        .queryParam("api_key", properties.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("🔥 AirLabs raw 응답 = " + raw);

        // 기존 DTO 파싱
        AirLabsResponseDto response = webClientBuilder.build() // todo : spring web flux 사용!
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("airlabs.co")
                        .path("/api/v9/flight")
                        .queryParam("flight_iata", flightIata)
                        .queryParam("api_key", properties.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(AirLabsResponseDto.class)
                .block();

        System.out.println("🔥 DTO 변환 결과 = " + response); // todo : logger 사용!!

        if (response == null || response.getResponse() == null) {
            throw new RuntimeException("항공편 정보를 찾을 수 없습니다."); // todo : error code 사용!!
        }

        var data = response.getResponse();

        return FlightInfoResponseDto.builder()
                .flightIata(data.getFlight_iata())
                .departureAirport(data.getDep_iata())
                .arrivalAirport(data.getArr_iata())
                .departureTime(data.getDep_time())
                .arrivalTime(data.getArr_time())
                .build();
    }
}