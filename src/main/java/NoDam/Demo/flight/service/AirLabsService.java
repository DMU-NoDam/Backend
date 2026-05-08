package NoDam.Demo.flight.service;

import NoDam.Demo.common.excetion.CustomException; // 추가
import NoDam.Demo.common.excetion.ErrorCode; // 추가
import NoDam.Demo.flight.config.AirLabsProperties;
import NoDam.Demo.flight.dto.AirLabsResponseDto;
import NoDam.Demo.flight.dto.FlightInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 추가
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j // 추가 : logger 사용
@Service
@RequiredArgsConstructor
public class AirLabsService {

    private final WebClient.Builder webClientBuilder;
    private final AirLabsProperties properties;

    public FlightInfoResponseDto getFlightInfo(String flightIata) {

        // 추가 : logger 사용
        log.info("AirLabs flight lookup request flightIata={}", flightIata);

        // 수정 : Spring WebFlux WebClient 사용 + API 1번만 호출
        AirLabsResponseDto response = webClientBuilder.build()
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

        // 수정 : RuntimeException -> CustomException + ErrorCode 사용
        if (response == null || response.getResponse() == null) {

            // 추가 : logger 사용
            log.warn("Flight not found. flightIata={}", flightIata);

            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        var data = response.getResponse();

        // 추가 : 시간 데이터 검증
        if (data.getDep_time() == null || data.getArr_time() == null) {

            // 추가 : logger 사용
            log.warn("Flight time is missing. flightIata={}", flightIata);

            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        return FlightInfoResponseDto.builder()
                .flightIata(data.getFlight_iata())
                .departureAirport(data.getDep_iata())
                .arrivalAirport(data.getArr_iata())
                .departureTime(data.getDep_time())
                .arrivalTime(data.getArr_time())
                .build();
    }
}