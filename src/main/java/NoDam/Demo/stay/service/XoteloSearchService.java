package NoDam.Demo.stay.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.stay.config.XoteloProperties;
import NoDam.Demo.stay.dto.XoteloSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class XoteloSearchService {

    private final WebClient.Builder webClientBuilder;
    private final XoteloProperties xoteloProperties;
    private final RegionQueryService regionQueryService;

    private static final Map<String, String> REGION_ENGLISH_NAME = Map.of(
            "TYO", "Tokyo",
            "OSA", "Osaka",
            "FUK", "Fukuoka",
            "SPK", "Sapporo",
            "KYO", "Kyoto",
            "OKI", "Okinawa"
    );

    public List<XoteloSearchResponseDto> searchStays(String regionCode) {
        log.info("Stay search request for regionCode={}", regionCode);

        String apiKey = xoteloProperties.getRapidApiKey();
        if (apiKey == null || apiKey.contains("${")) {
            log.error("CRITICAL ERROR: Xotelo API Key is NOT loaded!");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Region region = regionQueryService.findRegionsByCode(List.of(regionCode)).get(0);
        String cityName = REGION_ENGLISH_NAME.getOrDefault(regionCode, region.getName());

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(xoteloProperties.getSearchUrl())
                    .queryParam("query", cityName)
                    .build()
                    .toUri();

            log.info("Requesting Xotelo API: {}", uri);

            // 원복 : WebClient 의 기본 ObjectMapper 사용 (unknown property 무시 설정 포함)
            XoteloSearchResponseDto.RawResponse rawResponse = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", xoteloProperties.getRapidApiHost())
                    .retrieve()
                    .bodyToMono(XoteloSearchResponseDto.RawResponse.class)
                    .block();

            // 실제 데이터는 result.list에 들어있으므로 계층 구조에 맞춰 null 체크 및 접근
            if (rawResponse == null || rawResponse.getResult() == null || rawResponse.getResult().getList() == null) {
                log.warn("Xotelo API returned empty result for cityName={}", cityName);
                return Collections.emptyList();
            }

            return rawResponse.getResult().getList().stream()
                    .map(XoteloSearchResponseDto.RawResult::toDto)
                    .collect(Collectors.toList());

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Xotelo API Network Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unknown error in XoteloSearchService", e);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}
