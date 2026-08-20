package NoDam.Demo.plan.dto.response;

import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
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
    private Long orderIndex;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private PlaceInfo placeInfo;

    private TransportPlanInfo fromTransport;
    // private TransportPlanInfo arrivalTransport;

    public static PlacePlanInfo of(PlacePlan placePlan, Place place, TransportPlan fromTransport) {
        return PlacePlanInfo.builder()
                .id(placePlan.getId())
                .orderIndex(placePlan.getOrderIndex())
                .date(placePlan.getDatePlan().getDate())
                .startTime(placePlan.getStartTime())
                .endTime(placePlan.getEndTime())
                .placeInfo(place != null ? PlaceInfo.of(place) : PlaceInfo.empty())
                .fromTransport(fromTransport == null ? null : TransportPlanInfo.of(fromTransport))
//                .arrivalTransport(placePlan.getArrivalTransport() != null
//                        ? TransportPlanInfo.summary(placePlan.getArrivalTransport()) : null)
                .build();
    }
}
