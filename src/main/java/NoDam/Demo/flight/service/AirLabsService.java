package NoDam.Demo.flight.service;

import NoDam.Demo.common.excetion.CustomException; // 추가
import NoDam.Demo.common.excetion.ErrorCode; // 추가
import NoDam.Demo.common.util.DateUtil; // 추가 : 날짜 yyyy-MM-dd 변환
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

    // 사용자가 선택한 여행 시작일 ~ 종료일 범위 안에서 운항하는 항공편을 찾아 반환
    // - startDate, endDate 형식 : "yyyy-MM-dd"
    // - 범위 내 매칭되는 가장 빠른 운항편 1건 반환
    public FlightInfoResponseDto getFlightInfo(String flightIata, String startDate, String endDate) {
        // 정제 : 대문자 변환 및 공백 제거
        String cleanFlightIata = (flightIata != null) ? flightIata.toUpperCase().trim() : "";

        // 추가 : logger 사용
        log.info("AirLabs flight lookup request cleanFlightIata={}, startDate={}, endDate={}",
                cleanFlightIata, startDate, endDate);

        // 수정 : API 응답을 String으로 먼저 받아 로그를 찍은 뒤 파싱 (디버깅 강화)
        String rawResponse = webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("airlabs.co")
                        .path("/api/v9/schedules")
                        .queryParam("flight_iata", cleanFlightIata)
                        .queryParam("api_key", properties.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("AirLabs Raw Response for {}: {}", cleanFlightIata, rawResponse);

        // JSON 파싱 (ObjectMapper 또는 직접 변환)
        AirLabsResponseDto response;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
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
            log.warn("Empty schedule list from AirLabs for flightIata={}", cleanFlightIata);
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        // 수정 : 여행 날짜 범위(startDate ~ endDate) 안에서 운항하는 항공편 중 가장 빠른 1건 선택
        //        - dep_time 은 "yyyy-MM-dd HH:mm" 형식이라 앞 10자리(날짜)만 잘라 비교
        AirLabsResponseDto.FlightData data = response.getResponse().stream()
                .filter(f -> f.getDep_time() != null && f.getDep_time().length() >= 10)
                .filter(f -> {
                    String depDate = f.getDep_time().substring(0, 10); // "yyyy-MM-dd"
                    return depDate.compareTo(startDate) >= 0 && depDate.compareTo(endDate) <= 0;
                })
                .min(java.util.Comparator.comparing(AirLabsResponseDto.FlightData::getDep_time))
                .orElseThrow(() -> {
                    log.warn("Flight not found in trip date range. flightIata={}, startDate={}, endDate={}",
                            flightIata, startDate, endDate);
                    return new CustomException(ErrorCode.NOT_FOUND);
                });

        // 추가 : 시간 데이터 검증
        if (data.getDep_time() == null || data.getArr_time() == null) {

            // 추가 : logger 사용
            log.warn("Flight time is missing. flightIata={}", flightIata);

            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 추가 : dep_time, arr_time 의 날짜 부분만 yyyy-MM-dd 형식으로 변환 (DateUtil 사용)
        String depDate = DateUtil.fromLocalDate(
                DateUtil.toLocalDate(data.getDep_time().substring(0, 10))
        );
        String arrDate = DateUtil.fromLocalDate(
                DateUtil.toLocalDate(data.getArr_time().substring(0, 10))
        );

        return FlightInfoResponseDto.builder()
                .flightIata(data.getFlight_iata())
                .departureAirport(data.getDep_iata())
                .arrivalAirport(data.getArr_iata())
                .departureTime(depDate)   // 임시 : yyyy-MM-dd 형식
                .arrivalTime(arrDate)     // 임시 : yyyy-MM-dd 형식
                .build();
    }
}