package NoDam.Demo.plan.dto.response;

import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.plan.domain.PlacePlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacePlanInfo {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private PlaceInfo placeInfo;

    private TransportPlanInfo departureTransport;
    private TransportPlanInfo arrivalTransport;

    public static PlacePlanInfo of(PlacePlan placePlan, Place place) {
        return PlacePlanInfo.builder()
                .id(placePlan.getId())
                .date(placePlan.getDatePlan().getDate())
                .startTime(placePlan.getStartTime())
                .endTime(placePlan.getEndTime())
                .placeInfo(PlaceInfo.of(place))
                .departureTransport(placePlan.getDepartureTransport() != null
                        ? TransportPlanInfo.of(placePlan.getDepartureTransport()) : null)
                .arrivalTransport(placePlan.getArrivalTransport() != null
                        ? TransportPlanInfo.of(placePlan.getArrivalTransport()) : null)
                .build();
    }
}
