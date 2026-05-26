package NoDam.Demo.plan.dto.response;

import NoDam.Demo.plan.domain.TransportPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportPlanInfo {

    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer takeTime;
    private Integer totalDistanceMeters;
    private Long fromPlacePlanId;
    private Long toPlacePlanId;

    private Long transportPlanId;
    private RouteInfo routeInfo;

    public static TransportPlanInfo of(TransportPlan transport) {
        return TransportPlanInfo.builder()
                .id(transport.getId())
                .startTime(transport.getStartTime())
                .endTime(transport.getEndTime())
                .takeTime(transport.getTakeTime())
                .totalDistanceMeters(transport.getTotalDistanceMeters())
                .fromPlacePlanId(transport.getFromPlacePlan().getId())
                .toPlacePlanId(transport.getToPlacePlan().getId())
                .transportPlanId(transport.getId())
                .routeInfo(transport.getRouteInfo())
                .build();
    }

    public static TransportPlanInfo summary(TransportPlan transport) {
        return TransportPlanInfo.builder()
                .id(transport.getId())
                .startTime(transport.getStartTime())
                .endTime(transport.getEndTime())
                .takeTime(transport.getTakeTime())
                .totalDistanceMeters(transport.getTotalDistanceMeters())
                .fromPlacePlanId(transport.getFromPlacePlan().getId())
                .toPlacePlanId(transport.getToPlacePlan().getId())
                .transportPlanId(transport.getId())
                // .route()
                .build();
    }

    public static TransportPlanInfo of(RouteInfo route) {
        return TransportPlanInfo.builder()
                .takeTime(route.getTotalDurationSeconds())
                .totalDistanceMeters(route.getTotalDistanceMeters())
                .routeInfo(route)
                .build();
    }
}
