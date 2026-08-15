package NoDam.Demo.plan.behaviour.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Objects;

// PlacePlan을 바꾸는 한 번의 동작. BehaviourHistory에 JSON으로 저장되어 CAS 재적용에 쓰인다
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "behaviourType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddPlaceBehaviour.class, name = "ADD_PLACE"),
        @JsonSubTypes.Type(value = ChangePlaceBehaviour.class, name = "CHANGE_PLACE"),
        @JsonSubTypes.Type(value = RemovePlaceBehaviour.class, name = "REMOVE_PLACE"),
        @JsonSubTypes.Type(value = MovePlaceBehaviour.class, name = "MOVE_PLACE"),
        @JsonSubTypes.Type(value = FixPlaceBehaviour.class, name = "FIX_PLACE"),
})
public abstract class Behaviour {

    // 공통 규칙 1 : 같은 type이고 대상이 같으면 선행 실행이 우선이므로 이 behaviour는 버린다
    protected abstract boolean checkSameBehaviour(Behaviour previousBehaviour);

    // this가 @Param placePlanId를 옮겼다면 true, 아니면 false (삭제는 제외)
    protected abstract boolean isMoved(long placePlanId);

    // this가 @Param placePlanId를 삭제 했다면 true, 아니면 false (이동은 제외)
    protected abstract boolean isRemoved(long placePlanId);

    // 이웃 재계산은 DatePlan의 현재 순서를 읽어서 한다
    protected abstract void rebase(DatePlan datePlan, Behaviour previousBehaviour);

    // rebase 후에도 남아있는 id들이 이 DatePlan의 것인지 확인한다. apply 전에 호출한다
    public abstract void validate(DatePlan datePlan);

    // DatePlan의 placePlans를 고쳐 돌려준다. 저장은 호출부 책임
    public abstract DatePlan apply(DatePlan datePlan);

    public void rebase(DatePlan datePlan, PreviousBehaviours previousBehaviours) {
        if(previousBehaviours.isEmpty())
            return;

        for(Behaviour previousBehaviour : previousBehaviours.behaviours()) {
            if(checkSameBehaviour(previousBehaviour))
                throw new CustomException(ErrorCode.CONFLICT);
            rebase(datePlan, previousBehaviour);
        }
    }

}
