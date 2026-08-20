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

// 장소 고정 여부 변경
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FixPlaceBehaviour extends Behaviour {

    private Long placePlanId;
    private Boolean isFixed;

    @Override
    protected boolean checkSameBehaviour(Behaviour previousBehaviour) {
        if (!(previousBehaviour instanceof FixPlaceBehaviour fixPlaceBehaviour)) return false;

        return Objects.equals(placePlanId, fixPlaceBehaviour.getPlacePlanId());
    }

    // 고정 여부는 자리를 건드리지 않는다
    @Override
    protected boolean isMoved(long placePlanId) {
        return false;
    }

    @Override
    protected boolean isRemoved(long placePlanId) {
        return false;
    }

    @Override
    public void validate(DatePlan datePlan) {
        if (!datePlan.existPlacePlan(placePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);
    }

    // todo : PlacePlan에 고정 컬럼 추가 후 구현
    @Override
    public DatePlan apply(DatePlan datePlan) {
        return datePlan;
    }

    // 고정할 대상이 삭제되면 적용할 수 없다
    @Override
    protected void rebase(DatePlan datePlan, Behaviour previousBehaviour) {
        if (previousBehaviour.isRemoved(placePlanId))
            throw new CustomException(ErrorCode.CONFLICT);
    }

}
