package NoDam.Demo.plan.dto.ai;

import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.plan.dto.response.RouteInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendPlaceRequestDto {

    private ScheduleType scheduleType;
    private TripThemeType themeType;
    private String date; // yyyy-MM-dd

    private PlaceInfo previousPlace; // 이전 장소, null 이면 하루 첫 일정
    private PlaceInfo nextPlace;     // 다음 장소, null 이면 하루 마지막 일정

    private List<PlaceCandidate> candidates;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceCandidate {

        private RecommendPlaceResult place;
        private Integer travelTimeSeconds;
        private Integer travelDistanceMeters;

        public static PlaceCandidate of(RecommendPlaceResult place, RouteInfo route) {
            return PlaceCandidate.builder()
                    .place(place)
                    .travelTimeSeconds(route.getTotalDurationSeconds())
                    .travelDistanceMeters(route.getTotalDistanceMeters())
                    .build();
        }
    }
}
