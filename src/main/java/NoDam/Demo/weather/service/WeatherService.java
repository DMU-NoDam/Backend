package NoDam.Demo.weather.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.repository.RegionRepository;
import NoDam.Demo.weather.dto.OpenMeteoResponseDto;
import NoDam.Demo.weather.dto.WeatherRequestDto;
import NoDam.Demo.weather.dto.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final RegionRepository regionRepository;
    private final OpenMeteoService openMeteoService;
    private final ClimateService climateService;

    public WeatherResponseDto getForecast(WeatherRequestDto request) {
        // 이름으로 조회하되 중복 에러를 방지하기 위해 첫 번째 데이터를 선택
        Region region = regionRepository.findFirstByName(request.getCityName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (region.getLat() == null || region.getLon() == null) {
            log.error("Coordinates missing for city: {}", request.getCityName());
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), request.getStartDate());

        if (daysUntilStart > 7) {
            return climateService.getClimatePlaceholder(request.getCityName());
        }

        OpenMeteoResponseDto apiResponse = openMeteoService.getForecast(region.getLat(), region.getLon());
        return mapToWeatherResponse(request, apiResponse);
    }

    private WeatherResponseDto mapToWeatherResponse(WeatherRequestDto request, OpenMeteoResponseDto apiResponse) {
        List<WeatherResponseDto.DailyWeather> dailyForecasts = new ArrayList<>();
        
        if (apiResponse != null && apiResponse.getDaily() != null) {
            List<String> times = apiResponse.getDaily().getTime();
            
            for (int i = 0; i < times.size(); i++) {
                LocalDate date = LocalDate.parse(times.get(i));
                
                if ((date.isEqual(request.getStartDate()) || date.isAfter(request.getStartDate())) &&
                    (date.isEqual(request.getEndDate()) || date.isBefore(request.getEndDate()))) {
                    
                    dailyForecasts.add(WeatherResponseDto.DailyWeather.builder()
                            .date(date)
                            .maxTemperature(apiResponse.getDaily().getMaxTemp().get(i))
                            .minTemperature(apiResponse.getDaily().getMinTemp().get(i))
                            .weatherCode(apiResponse.getDaily().getWeatherCode().get(i))
                            .sourceType("FORECAST")
                            .build());
                }
            }
        }

        return WeatherResponseDto.builder()
                .cityName(request.getCityName())
                .forecast(dailyForecasts)
                .message("날씨 예보 조회 성공")
                .build();
    }
}
