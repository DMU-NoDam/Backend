package NoDam.Demo.plan.behaviour.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

// 장소 삭제
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RemovePlaceBehaviour extends Behaviour {

    private Long placePlanId;

    @Override
    protected boolean checkSameBehaviour(Behaviour previousBehaviour) {
        if (!(previousBehaviour instanceof RemovePlaceBehaviour removePlaceBehaviour)) return false;

        return Objects.equals(this.placePlanId, removePlaceBehaviour.getPlacePlanId());
    }

    @Override
    protected boolean isMoved(long placePlanId) {
        return false;
    }

    @Override
    protected boolean isRemoved(long placePlanId) {
        return Objects.equals(this.placePlanId, placePlanId);
    }

    @Override
    public void validate(DatePlan datePlan) {
        if (!datePlan.existPlacePlan(placePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);
    }

    @Override
    public DatePlan apply(DatePlan datePlan) {
        datePlan.removePlacePlan(placePlanId);

        return datePlan;
    }

    // 대상이 남아있는 한 삭제는 항상 성립한다 (place가 바뀌었든 자리가 바뀌었든 결과는 같음)
    @Override
    protected void rebase(DatePlan datePlan, Behaviour previousBehaviour) {
    }

}
