package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.DatePlan;

import java.util.List;
import java.util.Optional;

public interface DatePlanDBPort {

    // placePlans를 전량 함께 읽는다
    Optional<DatePlan> latestDatePlan(Long datePlanId);

    List<DatePlan> datePlans(Long tripId);

    // 메모리의 placePlans와 DB를 맞춘다. 목록에서 빠진 것은 삭제, 새로 들어온 것은 저장
    void saveDatePlan(DatePlan datePlan);

    void deleteDatePlan(DatePlan datePlan);

    void deleteDatePlans(List<DatePlan> datePlans);

}
