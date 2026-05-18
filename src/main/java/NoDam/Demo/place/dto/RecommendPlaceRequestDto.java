package NoDam.Demo.place.dto;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.WeatherType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RecommendPlaceRequestDto {

    private Long placePlanId;    // 교체 대상 place plan
    private PlaceType placeType; // null 이면 기존 place type 재사용

    private Double userLat;      // 사용자 위도
    private Double userLon;      // 사용자 경도

    private WeatherType weather; // 현재 날씨
    private LocalDateTime time;  // 요청 시간

}
