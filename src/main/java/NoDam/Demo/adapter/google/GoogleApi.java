package NoDam.Demo.adapter.google;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.adapter.google.dto.GooglePlaceInfo;
import NoDam.Demo.adapter.google.dto.GooglePlaceResponseDto;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class GoogleApi implements GooglePort {

    @Value("${external.google.api-key}")
    private String googleApiKey;

    @Value("${external.google.search-by-id}")
    private String googleSearchByIdUrl;

    @Value("${external.google.text-search-url}")
    private String googleTextSearchUrl;

    private static final String PLACE_FIELD_MASK = "places.id,places.types,places.location,places.formattedAddress,places.rating,places.googleMapsUri,places.regularOpeningHours.periods,places.websiteUri,places.displayName.text,places.parkingOptions.freeParkingLot,places.priceLevel,places.priceRange";

    private static final String PLACE_BY_ID_FIELD_MASK = "id,types,location,formattedAddress,rating,googleMapsUri,regularOpeningHours.periods,websiteUri,displayName.text,parkingOptions.freeParkingLot,priceLevel,priceRange";

    private final Logger logger = LoggerFactory.getLogger("google api service :: ");

    @Override
    public List<GooglePlaceInfo> searchByText(String hotelName) {
        try {
            Map<String, List<GooglePlaceResponseDto>> response = WebClient.create()
                    .post()
                    .uri(googleTextSearchUrl + "?key=" + googleApiKey)
                    .header("X-Goog-FieldMask", PLACE_FIELD_MASK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("textQuery", hotelName, "includedType", "lodging", "maxResultCount", 3))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        logger.error("Google API 4xx Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                        return Mono.error(new CustomException(ErrorCode.API_FAIL));
                                    })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, List<GooglePlaceResponseDto>>>() {})
                    .block();

            if (response == null || response.get("places") == null || response.get("places").isEmpty()) {
                return List.of();
            }

            logger.info("Google Text Search success for hotelName={}", hotelName);
            return response.get("places").stream()
                    .map(GooglePlaceResponseDto::toGooglePlaceInfo)
                    .toList();
        } catch (Exception e) {
            logger.error("Google Text Search failed for hotelName={}", hotelName, e);
            return List.of();
        }
    }

    @Override
    public GooglePlaceInfo searchByGoogleId(String googleId) {
        GooglePlaceResponseDto response = WebClient.create()
                .get()
                .uri(googleSearchByIdUrl + "/" + googleId + "?fields=" + PLACE_BY_ID_FIELD_MASK + "&key=" + googleApiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Google API 4xx Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new CustomException(ErrorCode.API_FAIL));
                                })
                )
                .bodyToMono(GooglePlaceResponseDto.class)
                .block();

        if (response == null) {
            return null;
        }

        logger.info(response.toString());

        return response.toGooglePlaceInfo();
    }

}
