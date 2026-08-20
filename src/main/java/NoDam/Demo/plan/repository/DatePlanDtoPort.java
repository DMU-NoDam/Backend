package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.dto.response.DatePlanInfo;
import NoDam.Demo.trip.domain.Trip;

import java.util.List;

// DatePlan 조회 응답 조립 output port
public interface DatePlanDtoPort {

    // 넘어온 DatePlan 기준으로 응답 dto를 만든다. place, region, trip은 DB에서 읽는다
    DatePlanInfo selectDatePlanInfo(DatePlan datePlan);

}
