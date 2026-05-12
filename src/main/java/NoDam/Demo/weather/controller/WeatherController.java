package NoDam.Demo.weather.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.weather.dto.WeatherRequestDto;
import NoDam.Demo.weather.dto.WeatherResponseDto;
import NoDam.Demo.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Weather", description = "날씨 정보 관련 API")
@RestController
@RequestMapping("/weather/public")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "날씨 예보 조회", description = "도시명과 날짜를 기준으로 실시간 예보 또는 기후 데이터를 조회합니다.")
    @GetMapping("/forecast")
    public ResponseEntity<SuccessResponse<WeatherResponseDto>> getForecast(@Valid @ModelAttribute WeatherRequestDto request) {
        return ResponseEntity.ok().body(new SuccessResponse<>("success", weatherService.getForecast(request)));
    }
}
