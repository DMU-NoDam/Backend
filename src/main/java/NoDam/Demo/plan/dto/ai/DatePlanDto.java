package NoDam.Demo.plan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
// used at create DatePlan with AI
public class DatePlanDto {

    private String date; // yyyy-mm-dd
    private String regionCode;
    private String googleIds; // todo : 정규화?

}
