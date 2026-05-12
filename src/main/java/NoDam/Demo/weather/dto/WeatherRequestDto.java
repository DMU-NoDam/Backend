package NoDam.Demo.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WeatherRequestDto {
    @NotBlank
    @Schema(description = "도시 이름 (예: 도쿄)", example = "도쿄")
    private String cityName;

    @NotNull
    @Schema(description = "여행 시작일", example = "2026-05-18")
    private LocalDate startDate;

    @NotNull
    @Schema(description = "여행 종료일", example = "2026-05-21")
    private LocalDate endDate;
}
