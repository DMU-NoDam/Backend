package NoDam.Demo.place.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.type.WeatherType;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceRequestDto;
import NoDam.Demo.place.service.PlaceFacadeService;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.weather.dto.OpenMeteoResponseDto;
import NoDam.Demo.weather.service.OpenMeteoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/place")
public class PlaceController {

    private final PlaceFacadeService placeFacadeService;
    private final OpenMeteoService openMeteoService;

    @PostMapping("/api/recommend")
    public ResponseEntity<SuccessResponse<List<PlaceInfo>>> recommendPlace(
            @RequestBody RecommendPlaceRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        WeatherType weather = resolveWeather(dto.getUserLat(), dto.getUserLon());
        List<PlaceInfo> result = placeFacadeService.recommendPlace(dto, user.getId(), weather);
        return ResponseEntity.ok(new SuccessResponse<List<PlaceInfo>>("success", result));
    }

    private WeatherType resolveWeather(Double lat, Double lon) {
        try {
            OpenMeteoResponseDto response = openMeteoService.getForecast(lat, lon);
            if (response == null || response.getDaily() == null || response.getDaily().getWeatherCode().isEmpty())
                return WeatherType.SUNNY;
            int code = response.getDaily().getWeatherCode().get(0);
            if ((code >= 71 && code <= 77) || code == 85 || code == 86) return WeatherType.SNOWY;
            if (code >= 50) return WeatherType.RAINY;
            return WeatherType.SUNNY;
        } catch (Exception e) {
            return WeatherType.SUNNY;
        }
    }
}
