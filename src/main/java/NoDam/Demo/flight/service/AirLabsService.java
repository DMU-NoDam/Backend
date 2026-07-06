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

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j // 추가 : logger 사용
@Service
@RequiredArgsConstructor
public class AirLabsService {

    private final WebClient.Builder webClientBuilder;
    private final AirLabsProperties properties;

    // 편명 + 여행 날짜로 운항 여부 확인 후 출발/도착 정보 반환
    // - /routes API : dep_time/arr_time 은 "HH:mm" 형식, days 로 운항 요일 확인
    public FlightInfoResponseDto getFlightInfo(String flightIata, String date) {
        // 정제 : 대문자 변환 및 공백 제거
        String cleanFlightIata = (flightIata != null) ? flightIata.toUpperCase().trim() : "";

        // 추가 : logger 사용
        log.info("AirLabs flight lookup request cleanFlightIata={}, date={}", cleanFlightIata, date);

        String rawResponse = webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("airlabs.co")
                        .path("/api/v9/routes")
                        .queryParam("flight_iata", cleanFlightIata)
                        .queryParam("api_key", properties.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("AirLabs Raw Response for {}: {}", cleanFlightIata, rawResponse);

        AirLabsResponseDto response;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            response = mapper.readValue(rawResponse, AirLabsResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to parse AirLabs response", e);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if (response == null || response.getResponse() == null) {
            log.warn("AirLabs API returned null response object for flightIata={}", cleanFlightIata);
            if (response != null && response.getError() != null) {
                log.warn("AirLabs API Error Details: {}", response.getError());
            }
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        if (response.getResponse().isEmpty()) {
            log.warn("Empty route list from AirLabs for flightIata={}", cleanFlightIata);
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        // date의 요일을 "mon", "tue" ... 형식으로 변환
        LocalDate localDate = LocalDate.parse(date);
        String dayOfWeek = localDate.getDayOfWeek().name().substring(0, 3).toLowerCase();

        AirLabsResponseDto.FlightData data = response.getResponse().stream()
                .filter(f -> f.getDays() != null && f.getDays().contains(dayOfWeek))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Flight not operating on {}. flightIata={}", dayOfWeek, flightIata);
                    return new CustomException(ErrorCode.NOT_FOUND);
                });

        if (data.getDep_time() == null || data.getArr_time() == null) {
            log.warn("Flight time is missing. flightIata={}", flightIata);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // dep_time, arr_time 이 "HH:mm" 형식 → 날짜와 합쳐 "yyyy-MM-dd HH:mm" 형성
        // arr_time < dep_time 이면 다음날 도착
        LocalTime depLocalTime = LocalTime.parse(data.getDep_time());
        LocalTime arrLocalTime = LocalTime.parse(data.getArr_time());
        String arrDate = arrLocalTime.isBefore(depLocalTime)
                ? localDate.plusDays(1).toString()
                : date;

        return FlightInfoResponseDto.builder()
                .flightIata(data.getFlight_iata())
                .departureAirport(AirportCode.from(data.getDep_iata()))
                .arrivalAirport(AirportCode.from(data.getArr_iata()))
                .departureTime(date + " " + data.getDep_time())
                .arrivalTime(arrDate + " " + data.getArr_time())
                .duration(data.getDuration())
                .build();
    }
}