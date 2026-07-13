package NoDam.Demo.plan.dto.response;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlanStatus;
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

    private PlanStatus planStatus; // trip의 DatePlan 중 가장 뒤처진 상태 (DatePlan이 없으면 null)
    private boolean allCompleted;
    private boolean isPlanning;

}
