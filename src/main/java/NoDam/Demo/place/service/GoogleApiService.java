package NoDam.Demo.place.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.place.dto.google.GooglePlaceInfo;
import NoDam.Demo.place.dto.google.GooglePlaceResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class GoogleApiService {

    @Value("${external.google.api-key}")
    private String googleApiKey;

    @Value("${external.google.search-by-id}")
    private String googleSearchByIdUrl;

    private static final String PLACE_FIELD_MASK = "places.id,places.types,places.location,places.formattedAddress,places.rating,places.googleMapsUri,places.regularOpeningHours.periods,places.websiteUri,places.displayName.text,places.parkingOptions.freeParkingLot,places.priceLevel,places.priceRange";

    private final Logger logger = LoggerFactory.getLogger("google api service :: ");

    public GooglePlaceInfo searchByGoogleId(String googleId) {
        GooglePlaceResponseDto response = WebClient.create()
                .get()
                .uri(googleSearchByIdUrl + "/" + googleId + "?fields=" + PLACE_FIELD_MASK + "&key=" + googleApiKey)
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
