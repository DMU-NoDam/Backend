package NoDam.Demo.weather.service;

import NoDam.Demo.weather.dto.WeatherResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class ClimateService {

    public WeatherResponseDto getClimatePlaceholder(String cityName) {
        log.info("Returning climate placeholder for city: {}", cityName);
        return WeatherResponseDto.builder()
                .cityName(cityName)
                .forecast(new ArrayList<>())
                .message("여행 시작일이 7일을 초과하여 월별 기후 데이터 조회 예정입니다.")
                .build();
    }
}
