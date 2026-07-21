package NoDam.Demo.adapter.hotel;

import NoDam.Demo.adapter.google.GooglePort;
import NoDam.Demo.adapter.google.dto.GooglePlaceInfo;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.place.domain.Coordinate;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.stay.config.XoteloProperties;
import NoDam.Demo.stay.domain.XoteloRegionCode;
import NoDam.Demo.stay.dto.XoteloSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Xotelo 숙소 검색 + google 장소 매칭으로 호텔을 추천하는 adapter
@Slf4j
@Component
@RequiredArgsConstructor
public class XoteloHotelAdapter implements HotelPort {

    private final WebClient.Builder webClientBuilder;
    private final XoteloProperties xoteloProperties;
    private final GooglePort googlePort;

    @Override
    public Optional<String> recommendHotelGoogleId(Region region) {
        List<XoteloSearchResponseDto> xotelList = searchStays(region)
                .stream()
                .filter(e-> e != null && e.getName() != null)
                .toList();

        // lat lon 이 있는 호텔들 먼저
        for (XoteloSearchResponseDto hotelInfo : xotelList) {
            if(hotelInfo.getLatitude() == null || hotelInfo.getLongitude() == null)
                continue;

            List<GooglePlaceInfo> googleResults = googlePort.searchByText(hotelInfo.getName());
            for(GooglePlaceInfo placeInfo : googleResults) {
                if (Coordinate.isSameLocation(hotelInfo.getLatitude(), hotelInfo.getLongitude(), placeInfo.getLat(), placeInfo.getLon()))
                    return Optional.ofNullable(placeInfo.getPlaceId());
            }
        }

        // lat lon이 없는 호텔들
        for (XoteloSearchResponseDto hotelInfo : xotelList) {
            if (hotelInfo.getLatitude() != null && hotelInfo.getLongitude() != null)
                continue; // 위에서 시도함

            List<GooglePlaceInfo> googleResults = googlePort.searchByText(hotelInfo.getName());
            if (googleResults.isEmpty()) continue;

            return Optional.ofNullable(googleResults.get(0).getPlaceId());
        }

        return Optional.empty();
    }

    private List<XoteloSearchResponseDto> searchStays(Region region) {
        log.info("Stay search request for regionCode={}", region.getName());

        String apiKey = xoteloProperties.getRapidApiKey();
        if (apiKey == null || apiKey.contains("${")) {
            log.error("CRITICAL ERROR: Xotelo API Key is NOT loaded!");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String cityName = XoteloRegionCode.getByRegionCode(region.getCode()).getXoteloCode();

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(xoteloProperties.getSearchUrl())
                    .queryParam("location_key", cityName)
                    .build()
                    .toUri();

            log.info("Requesting Xotelo API: {}", uri);

            XoteloSearchResponseDto.RawResponse rawResponse = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(XoteloSearchResponseDto.RawResponse.class)
                    .block();

            // 실제 데이터는 result.list에 들어있으므로 계층 구조에 맞춰 null 체크 및 접근
            if (rawResponse == null || rawResponse.getResult() == null || rawResponse.getResult().getList() == null) {
                log.warn("Xotelo API returned empty result for locationKey={}", cityName);
                return Collections.emptyList();
            }

            return rawResponse.getResult().getList().stream()
                    .map(XoteloSearchResponseDto.RawResult::toDto)
                    .collect(Collectors.toList());

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            log.error("Xotelo API Network Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unknown error in XoteloHotelAdapter", e);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}
