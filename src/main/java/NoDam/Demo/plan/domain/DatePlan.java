package NoDam.Demo.plan.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// 하루치 plan list domain (일급 컬렉션)
@NoArgsConstructor
@Getter
@Setter
public class DatePlan {

    private List<Plan> plans;

    public DatePlan (List<Plan> plans) {
        this.plans = plans;
    }

}
