package NoDam.Demo.place.dto;

import NoDam.Demo.plan.dto.response.RouteInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPlaceInfo {

    private PlaceInfo place;
    private Integer travelDurationSeconds;
    private Integer travelDistanceMeters;
    private LocalTime startTime;
    private LocalTime endTime;

    public static RecommendedPlaceInfo of(PlaceInfo place, RouteInfo route, LocalTime startTime, LocalTime endTime) {
        return RecommendedPlaceInfo.builder()
                .place(place)
                .travelDurationSeconds(route.getTotalDurationSeconds())
                .travelDistanceMeters(route.getTotalDistanceMeters())
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

}
