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

// 장소 변경. 자리(orderIndex)는 그대로 두고 placeId만 바꾼다
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePlaceBehaviour extends Behaviour {

    private Long placePlanId;
    private Long placeId;

    @Override
    protected boolean checkSameBehaviour(Behaviour previousBehaviour) {
        if (!(previousBehaviour instanceof ChangePlaceBehaviour changePlaceBehaviour)) return false;

        return Objects.equals(placePlanId, changePlaceBehaviour.getPlacePlanId());
    }

    // 장소 변경은 자리를 건드리지 않는다
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

    @Override
    public DatePlan apply(DatePlan datePlan) {
        datePlan.changePlaceId(placePlanId, placeId);

        return datePlan;
    }

    // 바꿀 대상이 삭제되면 적용할 수 없다 (이동은 자리만 바뀌므로 그대로 적용 가능)
    @Override
    protected void rebase(DatePlan datePlan, Behaviour previousBehaviour) {
        if (previousBehaviour.isRemoved(placePlanId))
            throw new CustomException(ErrorCode.CONFLICT);
    }

}
