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

// 장소 추가. previous, next 사이에 넣는다 (맨 앞이면 previous, 맨 뒤면 next가 null)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE) // json 역직렬화용
public class AddPlaceBehaviour extends Behaviour {

    private Long previousPlacePlanId;
    private Long nextPlacePlanId;
    private Long placeId;

    public AddPlaceBehaviour(
            Long previousPlacePlanId,
            Long nextPlacePlanId,
            Long placeId
    ) {
        if((previousPlacePlanId == null && nextPlacePlanId == null) || placeId == null)
            throw new CustomException(ErrorCode.CONFLICT);

        this.previousPlacePlanId = previousPlacePlanId;
        this.nextPlacePlanId = nextPlacePlanId;
        this.placeId = placeId;
    }

    // 같은 자리(previous, next 모두 동일)에 넣는 경우만 같은 behaviour로 본다
    @Override
    protected boolean checkSameBehaviour(Behaviour previousBehaviour) {
        if (!(previousBehaviour instanceof AddPlaceBehaviour addPlaceBehaviour)) return false;

        return Objects.equals(previousPlacePlanId, addPlaceBehaviour.getPreviousPlacePlanId())
                && Objects.equals(nextPlacePlanId, addPlaceBehaviour.getNextPlacePlanId());
    }

    // 추가는 기존 PlacePlan의 자리를 건드리지 않는다
    @Override
    protected boolean isMoved(long placePlanId) {
        return false;
    }

    @Override
    protected boolean isRemoved(long placePlanId) {
        return false;
    }

    // 기준으로 삼은 이웃이 이 DatePlan의 것이어야 한다 (null은 맨 앞/맨 뒤를 뜻하므로 검사 대상이 아니다)
    @Override
    public void validate(DatePlan datePlan) {
        if (previousPlacePlanId != null && !datePlan.existPlacePlan(previousPlacePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);

        if (nextPlacePlanId != null && !datePlan.existPlacePlan(nextPlacePlanId))
            throw new CustomException(ErrorCode.NOT_FOUND);
    }

    @Override
    public DatePlan apply(DatePlan datePlan) {
        datePlan.addPlacePlan(placeId, previousPlacePlanId, nextPlacePlanId);

        return datePlan;
    }

    // 기준으로 삼은 이웃이 삭제되거나 이동하면 남은 쪽 기준으로 자리를 다시 잡는다
    @Override
    protected void rebase(DatePlan datePlan, Behaviour previousBehaviour) {
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

        // 기준이 하나도 남지 않으면 넣을 자리를 잃는다 (빈 DatePlan에 처음 넣는 경우와 구분한다)
        if (previousPlacePlanId == null && nextPlacePlanId == null)
            throw new CustomException(ErrorCode.CONFLICT);
    }

}
