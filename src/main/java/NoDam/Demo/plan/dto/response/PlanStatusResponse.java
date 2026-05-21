package NoDam.Demo.plan.dto.response;

import NoDam.Demo.common.type.PlanStatus;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.trip.domain.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanStatusResponse {

    private boolean allCompleted;
    private boolean isPlanning;

}
