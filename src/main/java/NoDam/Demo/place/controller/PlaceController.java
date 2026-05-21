package NoDam.Demo.place.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.type.WeatherType;
import NoDam.Demo.place.dto.RecommendPlaceRequestDto;
import NoDam.Demo.place.dto.RecommendedPlaceInfo;
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
    public ResponseEntity<SuccessResponse<List<RecommendedPlaceInfo>>> recommendPlace(
            @RequestBody RecommendPlaceRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        List<RecommendedPlaceInfo> result = placeFacadeService.recommendPlace(dto, user.getId(), WeatherType.SUNNY);
        return ResponseEntity.ok(new SuccessResponse<List<RecommendedPlaceInfo>>("success", result));
    }
}
