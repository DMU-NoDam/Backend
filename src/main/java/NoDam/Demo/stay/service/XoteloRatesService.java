package NoDam.Demo.stay.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.stay.config.XoteloProperties;
import NoDam.Demo.stay.dto.XoteloRatesResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xotelo /api/rates 호출 전용 서비스
 * - hotelKey + 체크인/체크아웃 날짜로 OTA 별 가격 조회
 * - data.xotelo.com 직접 호출 (RapidAPI 미경유 → 인증 헤더 불필요)
 * - 통화 : Xotelo 응답 통화 (USD) 그대로 반환. KRW 변환은 추후 단계
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XoteloRatesService {

    private final WebClient.Builder webClientBuilder;
    private final XoteloProperties xoteloProperties;

    public List<XoteloRatesResponseDto> getRates(String hotelKey, String checkIn, String checkOut) {
        log.info("Stay rates request hotelKey={}, checkIn={}, checkOut={}", hotelKey, checkIn, checkOut);

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(xoteloProperties.getRatesUrl())
                    .queryParam("hotel_key", hotelKey)
                    .queryParam("chk_in", checkIn)
                    .queryParam("chk_out", checkOut)
                    .build()
                    .toUri();

            log.info("Requesting Xotelo Rates API: {}", uri);

            // data.xotelo.com 직호출 → 인증 헤더 없이 호출
            XoteloRatesResponseDto.RawResponse rawResponse = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(XoteloRatesResponseDto.RawResponse.class)
                    .block();

            if (rawResponse == null
                    || rawResponse.getResult() == null
                    || rawResponse.getResult().getRates() == null) {
                log.warn("Xotelo Rates API returned empty rates. hotelKey={}", hotelKey);
                return Collections.emptyList();
            }

            String currency = rawResponse.getResult().getCurrency(); // 응답 통화 (보통 "USD")

            return rawResponse.getResult().getRates().stream()
                    .map(rawRate -> rawRate.toDto(currency))
                    .collect(Collectors.toList());

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Xotelo Rates API Network Error: Status={}, Body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unknown error in XoteloRatesService", e);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}
