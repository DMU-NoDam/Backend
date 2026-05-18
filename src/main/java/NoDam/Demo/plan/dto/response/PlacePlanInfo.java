package NoDam.Demo.plan.dto.response;

import NoDam.Demo.plan.domain.PlacePlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacePlanInfo {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long placeId;

    private TransportPlanInfo departureTransport;
    private TransportPlanInfo arrivalTransport;

    public static PlacePlanInfo of(PlacePlan placePlan) {
        return PlacePlanInfo.builder()
                .id(placePlan.getId())
                .date(placePlan.getDatePlan().getDate())
                .startTime(placePlan.getStartTime())
                .endTime(placePlan.getEndTime())
                .placeId(placePlan.getPlaceId())
                .departureTransport(placePlan.getDepartureTransport() != null
                        ? TransportPlanInfo.of(placePlan.getDepartureTransport()) : null)
                .arrivalTransport(placePlan.getArrivalTransport() != null
                        ? TransportPlanInfo.of(placePlan.getArrivalTransport()) : null)
                .build();
    }
}
