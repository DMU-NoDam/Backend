package NoDam.Demo.exchange.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.exchange.config.FreeCurrencyProperties;
import NoDam.Demo.exchange.dto.ExchangeRateResponseDto;
import NoDam.Demo.exchange.dto.FreeCurrencyResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 환율 조회 (FreeCurrencyAPI)
 * 무료 키는 월 호출 한도가 있어서, 통화 쌍마다 1시간 동안 메모리에 보관하고 그 사이 요청은 캐시로 응답한다.
 * → 사용자 수와 관계없이 통화 쌍당 최대 1시간에 1번만 외부 API를 호출한다.
 * 동시 요청이 캐시 만료 시점에 겹치면 외부 호출이 몇 번 중복될 수 있지만 한도에 영향이 작아 락을 두지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private static final Duration API_TIMEOUT = Duration.ofSeconds(5);
    private static final Pattern CURRENCY_CODE = Pattern.compile("^[A-Z]{3}$");

    private final WebClient.Builder webClientBuilder;
    private final FreeCurrencyProperties properties;

    private final Map<String, ExchangeRateResponseDto> cache = new ConcurrentHashMap<>();

    public ExchangeRateResponseDto getRate(String base, String target) {
        String cleanBase = normalize(base);
        String cleanTarget = normalize(target);

        String key = cleanBase + ":" + cleanTarget;
        ExchangeRateResponseDto cached = cache.get(key);
        if (cached != null && !isExpired(cached))
            return cached;

        try {
            ExchangeRateResponseDto fresh = fetchRate(cleanBase, cleanTarget);
            cache.put(key, fresh);
            return fresh;
        } catch (CustomException e) {
            // 외부 API 장애 시 만료된 값이라도 있으면 그것을 돌려준다 (updatedAt으로 오래된 값임을 알 수 있음)
            if (cached != null) {
                log.warn("Exchange rate refresh failed, returning stale cache. key={}", key);
                return cached;
            }
            throw e;
        }
    }

    private ExchangeRateResponseDto fetchRate(String base, String target) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.error("FREECURRENCY_API_KEY is not configured");
            throw new CustomException(ErrorCode.API_FAIL);
        }

        URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .queryParam("apikey", properties.getApiKey())
                .queryParam("base_currency", base)
                .queryParam("currencies", target)
                .build()
                .toUri();

        FreeCurrencyResponseDto response;
        try {
            response = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(FreeCurrencyResponseDto.class)
                    .block(API_TIMEOUT);
        } catch (Exception e) {
            // uri에 api 키가 들어 있으므로 예외 메시지(uri 포함 가능)를 그대로 남기지 않는다
            log.error("FreeCurrencyAPI call failed. base={}, target={}, error={}", base, target, e.getClass().getSimpleName());
            throw new CustomException(ErrorCode.API_FAIL);
        }

        Double rate = (response != null && response.getData() != null) ? response.getData().get(target) : null;
        if (rate == null) {
            log.warn("FreeCurrencyAPI returned no rate. base={}, target={}", base, target);
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        log.info("Exchange rate fetched. base={}, target={}, rate={}", base, target, rate);
        return ExchangeRateResponseDto.builder()
                .base(base)
                .target(target)
                .rate(rate)
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    private boolean isExpired(ExchangeRateResponseDto rate) {
        return System.currentTimeMillis() - rate.getUpdatedAt() > CACHE_TTL.toMillis();
    }

    // 정제 : 대문자 변환 및 공백 제거, 3자리 통화 코드가 아니면 거부
    private String normalize(String currency) {
        String clean = (currency != null) ? currency.toUpperCase().trim() : "";
        if (!CURRENCY_CODE.matcher(clean).matches())
            throw new CustomException(ErrorCode.INVALID_REQUEST_PARAMETER);
        return clean;
    }
}
