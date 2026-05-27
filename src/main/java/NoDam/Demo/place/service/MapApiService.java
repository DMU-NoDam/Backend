package NoDam.Demo.place.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.google.GoogleRouteRequestDto;
import NoDam.Demo.place.dto.google.GoogleRouteResponseDto;
import NoDam.Demo.place.dto.navitime.NavitimeRouteResponseDto;
import NoDam.Demo.ai.AiService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MapApiService {

    private final GoogleApiService googleApiService;
    private final AiService aiService;


    @Value("${external.navitime.base-url}")
    private String navitimeBaseUrl;

    @Value("${external.navitime.rapid-api-key}")
    private String navitimeRapidApiKey;

    @Value("${external.google.routes-url}")
    private String googleRoutesUrl;

    @Value("${external.google.api-key}")
    private String googleApiKey;

    private static final String NAVITIME_RAPID_API_HOST = "navitime-route-totalnavi.p.rapidapi.com";
    private static final String GOOGLE_ROUTE_FIELD_MASK = "routes.distanceMeters,routes.staticDuration,routes.polyline.encodedPolyline,routes.legs.distanceMeters,routes.legs.staticDuration,routes.legs.polyline.encodedPolyline,routes.legs.startLocation,routes.legs.endLocation,routes.legs.steps.distanceMeters,routes.legs.steps.staticDuration,routes.legs.steps.polyline.encodedPolyline,routes.legs.steps.startLocation,routes.legs.steps.endLocation,routes.legs.steps.travelMode,routes.legs.steps.transitDetails.stopDetails,routes.legs.steps.transitDetails.transitLine.name,routes.legs.steps.transitDetails.transitLine.nameShort,routes.legs.steps.transitDetails.transitLine.vehicle.type,routes.legs.steps.transitDetails.stopCount";
    private static final String GOOGLE_ROUTE_SUMMARY_FIELD_MASK = "routes.distanceMeters,routes.staticDuration";
    private static final DateTimeFormatter NAVITIME_START_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Logger logger = LoggerFactory.getLogger("map api service :: ");

    public RouteInfo computeRoutesNavitimeFromCoord(Double startLat, Double startLon, PlaceInfo end, LocalTime startTime) {
        if(startLat == null || startLon == null)
            return RouteInfo.empty();

        RouteInfo routes = callNavitime(startLat, startLon, end.getLat(), end.getLon(), startTime);
        translateRouteNames(routes, "ja", "ko");
        return routes;
    }

    public RouteInfo computeRoutesNavitimeFromPlace(Place start, Place end, LocalTime startTime) {
        RouteInfo routes = callNavitime(start.getLat(), start.getLon(), end.getLat(), end.getLon(), startTime);
        routes.setStartAndEndName(start.getName(), end.getName());
        translateRouteNames(routes, "ja", "ko");
        return routes;
    }

    private RouteInfo callNavitime(Double startLat, Double startLon, Double endtLat, Double endLon, LocalTime startTime) {
        String startCoord = startLat + "," + startLon;
        String goalCoord = endtLat + "," + endLon;
        String startTimeStr = LocalDateTime.of(LocalDate.now(), startTime).format(NAVITIME_START_TIME_FORMATTER);

        NavitimeRouteResponseDto response = WebClient.create()
                .get()
                .uri(navitimeBaseUrl + "/route_transit", uriBuilder -> uriBuilder
                        .queryParam("start", startCoord)
                        .queryParam("goal", goalCoord)
                        .queryParam("datum", "wgs84")
                        .queryParam("term", 1440)
                        .queryParam("options", "railway_calling_at")
                        .queryParam("limit", 1)
                        .queryParam("start_time", startTimeStr)
                        .queryParam("coord_unit", "degree")
                        .build()
                )
                .header("x-rapidapi-host", NAVITIME_RAPID_API_HOST)
                .header("x-rapidapi-key", navitimeRapidApiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("Navitime API 4xx Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new CustomException(ErrorCode.API_FAIL));
                                })
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                logger.error("Navitime API 5xx Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new CustomException(ErrorCode.API_FAIL));
                            })
                )
                .bodyToMono(NavitimeRouteResponseDto.class)
                .block();

        RouteInfo routeInfo = response.toRouteInfo();

        logger.info("navitime route api response {}", routeInfo);

        return routeInfo;
    }

    private RouteInfo translateRouteNames(RouteInfo routeInfo, String sourceLang, String targetLang) {
        if (routeInfo == null || routeInfo.getSteps() == null || routeInfo.getSteps().isEmpty()) {
            return routeInfo;
        }

        List<RouteInfo.Point> allPoints = routeInfo.getSteps().stream()
                .flatMap(step -> Stream.of(step.getStart(), step.getEnd()))
                .filter(point -> point != null && point.getName() != null)
                .toList();

        if (allPoints.isEmpty()) return routeInfo;

        List<String> names = allPoints.stream().map(RouteInfo.Point::getName).toList();
        List<String> translated = aiService.translate(names, sourceLang, targetLang);

        for (int i = 0; i < allPoints.size(); i++) {
            allPoints.get(i).setName(translated.get(i));
        }

        return routeInfo;
    }

    private RouteInfo callGoogleRoute(Double startLat, Double startLon, Place end, LocalTime startTime) {
        GoogleRouteRequestDto requestBody = GoogleRouteRequestDto.transit(startLat, startLon, end.getLat(), end.getLon(), startTime);

        GoogleRouteResponseDto response = WebClient.create()
                .post()
                .uri(googleRoutesUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", googleApiKey)
                .header("X-Goog-FieldMask", GOOGLE_ROUTE_FIELD_MASK)
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

        return response != null ? response.toRouteInfo() : null;
    }

    private RouteInfo callGoogleSummary(Double originLat, Double originLon, Place end, LocalTime startTime) {
        GoogleRouteRequestDto requestBody = GoogleRouteRequestDto.transit(originLat, originLon, end.getLat(), end.getLon(), startTime);

        GoogleRouteResponseDto response = WebClient.create()
                .post()
                .uri(googleRoutesUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", googleApiKey)
                .header("X-Goog-FieldMask", GOOGLE_ROUTE_SUMMARY_FIELD_MASK)
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

        logger.info("google route summary api response: {}", response);

        return response != null ? response.toRouteSummary() : null;
    }

}
