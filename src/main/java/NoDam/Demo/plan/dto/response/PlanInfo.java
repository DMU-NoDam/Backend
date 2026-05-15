package NoDam.Demo.plan.dto.response;

import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.Plan;
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
public class PlanInfo {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    // PlacePlan fields
    private Long placeId;

    // TransportPlan fields
    private Integer takeTime;
    private Long toPlaceId;
    private Long fromPlaceId;
    private String googleId;

    private String planType; // "PLACE" or "TRANSPORT"

    public static PlanInfo of(Plan plan) {
        PlanInfoBuilder builder = PlanInfo.builder()
                .id(plan.getId())
                .date(plan.getDatePlan().getDate())
                .startTime(plan.getStartTime())
                .endTime(plan.getEndTime());

        if (plan instanceof PlacePlan placePlan) {
            builder.placeId(placePlan.getPlaceId())
                    .planType("PLACE");
        } else if (plan instanceof TransportPlan transportPlan) {
            builder.takeTime(transportPlan.getTakeTime())
                    .toPlaceId(transportPlan.getToPlaceId())
                    .fromPlaceId(transportPlan.getFromPlaceId())
                    .googleId(transportPlan.getGoogleId())
                    .planType("TRANSPORT");
        }

        return builder.build();
    }
}
