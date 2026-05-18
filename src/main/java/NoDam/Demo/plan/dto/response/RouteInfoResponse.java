package NoDam.Demo.plan.dto.response;

import NoDam.Demo.place.domain.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RouteInfoResponse {

    private Integer totalDistanceMeters;
    private Integer totalDurationSeconds;
    private List<RouteStepResponse> steps;

    @Getter
    @AllArgsConstructor
    public static class RouteStepResponse {
        private Coordinate start;
        private Coordinate end;
        private List<Coordinate> polyline;
        private Integer distanceMeters;
        private Integer durationSeconds;
        private String travelMode;
        private RouteInfo.TransitInfo transitInfo;
    }
}
