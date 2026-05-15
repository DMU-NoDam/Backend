package NoDam.Demo.place.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.place.dto.google.GooglePlaceInfo;
import NoDam.Demo.place.dto.google.GooglePlaceResponseDto;
import NoDam.Demo.place.dto.google.GoogleRouteRequestDto;
import NoDam.Demo.place.dto.google.GoogleRouteResponseDto;
import NoDam.Demo.plan.dto.response.RouteInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleApiService {

    @Value("${external.google.api-key}")
    private String GOOGLE_API_KEY;

    @Value("${external.google.search-by-id}")
    private String GOOGLE_SEARCH_BY_ID_URI;

    @Value("${external.google.routes-url}")
    private String GOOGLE_ROUTES_URI;

    // 전역 변수들
    private static final String PLACE_FIELD_MASK = "places.id,places.types,places.location,places.formattedAddress,places.rating,places.googleMapsUri,places.regularOpeningHours.periods,places.websiteUri,places.displayName.text,places.parkingOptions.freeParkingLot,places.priceLevel,places.priceRange";
    private static final List<String> PLACE_INCLUDED_TYPES = List.of("art_gallery","museum","zoo","park","plaza","garden","tourist_attraction","aquarium","cafe","restaurant","bakery","bar","cafeteria","pub","bed_and_breakfast","budget_japanese_inn","campground","camping_cabin","cottage","farmstay","guest_house","hostel","hotel","inn","japanese_inn","lodging","motel","private_guest_room","resort_hotel","market","store","supermarket","ski_resort","fishing_pond","golf_course","airport","hypermarket");
    private static final String ROUTE_FIELD_MASK = "routes.distanceMeters,routes.staticDuration,routes.polyline.encodedPolyline,routes.legs.distanceMeters,routes.legs.staticDuration,routes.legs.polyline.encodedPolyline,routes.legs.startLocation,routes.legs.endLocation,routes.legs.steps.distanceMeters,routes.legs.steps.staticDuration,routes.legs.steps.polyline.encodedPolyline,routes.legs.steps.startLocation,routes.legs.steps.endLocation,routes.legs.steps.travelMode,routes.legs.steps.transitDetails.stopDetails,routes.legs.steps.transitDetails.transitLine.name,routes.legs.steps.transitDetails.transitLine.nameShort,routes.legs.steps.transitDetails.transitLine.vehicle.type,routes.legs.steps.transitDetails.stopCount";

    private final Logger logger = LoggerFactory.getLogger("google api service :: ");

    public GooglePlaceInfo searchByGoogleId(String googleId) {
        GooglePlaceResponseDto response = WebClient.create()
                .get()
                .uri(GOOGLE_SEARCH_BY_ID_URI + "/" + googleId + "?fields=" + PLACE_FIELD_MASK + "&key=" + GOOGLE_API_KEY)
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

    public RouteInfo computeRoutes(Double originLat, Double originLng, Double destinationLat, Double destinationLng) {
        GoogleRouteRequestDto requestBody = GoogleRouteRequestDto.transit(originLat, originLng, destinationLat, destinationLng);

        GoogleRouteResponseDto response = WebClient.create()
                .post()
                .uri(GOOGLE_ROUTES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", GOOGLE_API_KEY)
                .header("X-Goog-FieldMask", ROUTE_FIELD_MASK)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Google API 4xx Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new CustomException(ErrorCode.API_FAIL));
                                })
                )
                .bodyToMono(GoogleRouteResponseDto.class)
                .block();

        logger.info("google route api response: {}", response);

        return response.toRouteInfo();
    }

}
