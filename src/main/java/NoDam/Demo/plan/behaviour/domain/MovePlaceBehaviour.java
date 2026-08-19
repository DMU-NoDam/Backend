package NoDam.Demo.plan.behaviour.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

// 순서 변경. previous, next 사이로 옮긴다 (맨 앞이면 previous, 맨 뒤면 next가 null)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE) // json 역직렬화용
public class MovePlaceBehaviour extends Behaviour {

    private Long placePlanId;
    private Long previousPlacePlanId;
    private Long nextPlacePlanId;

    public MovePlaceBehaviour(
            Long placePlanId,
            Long previousPlacePlanId,
            Long nextPlacePlanId
    ) {
        if (placePlanId == null || (previousPlacePlanId == null && nextPlacePlanId == null))
            throw new CustomException(ErrorCode.CONFLICT);

        this.placePlanId = placePlanId;
        this.previousPlacePlanId = previousPlacePlanId;
        this.nextPlacePlanId = nextPlacePlanId;
    }

    @Override
    protected boolean checkSameBehaviour(Behaviour previousBehaviour) {
        if (!(previousBehaviour instanceof MovePlaceBehaviour movePlaceBehaviour)) return false;

        return Objects.equals(placePlanId, movePlaceBehaviour.getPlacePlanId());
    }

    @Override
    protected boolean isMoved(long placePlanId) {
        return Objects.equals(this.placePlanId, placePlanId);
    }

    @Override
    protected boolean isRemoved(long placePlanId) {
        return false;
    }

    // 옮길 대상과 기준으로 삼은 이웃이 이 DatePlan의 것이어야 한다 (null은 맨 앞/맨 뒤)
    @Override
    public void validate(DatePlan datePlan) {
        if (!datePlan.existPlacePlan(placePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);

        if (previousPlacePlanId != null && !datePlan.existPlacePlan(previousPlacePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);

        if (nextPlacePlanId != null && !datePlan.existPlacePlan(nextPlacePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);
    }

    @Override
    public DatePlan apply(DatePlan datePlan) {
        datePlan.movePlacePlan(placePlanId, previousPlacePlanId, nextPlacePlanId);

        return datePlan;
    }

    // 옮길 대상이 삭제되면 적용할 수 없다
    // 목적지로 삼은 이웃이 삭제되거나 이동하면 남은 쪽 기준으로 자리를 다시 잡는다
    @Override
    protected void rebase(DatePlan datePlan, Behaviour previousBehaviour) {
        if (previousBehaviour.isRemoved(placePlanId))
            throw new CustomException(ErrorCode.CONFLICT);

        if (nextPlacePlanId != null) {
            if (previousBehaviour.isRemoved(nextPlacePlanId) || previousBehaviour.isMoved(nextPlacePlanId)) {
                nextPlacePlanId = datePlan.findNextPlacePlan(previousPlacePlanId).getId();
            }
        }

        if (previousPlacePlanId != null) {
            if (previousBehaviour.isRemoved(previousPlacePlanId) || previousBehaviour.isMoved(previousPlacePlanId)) {
                previousPlacePlanId = datePlan.findPreviousPlacePlan(nextPlacePlanId).getId();
            }
        }

        // 기준이 하나도 남지 않으면 옮길 자리를 잃는다
        if (previousPlacePlanId == null && nextPlacePlanId == null)
            throw new CustomException(ErrorCode.CONFLICT);
    }

}
