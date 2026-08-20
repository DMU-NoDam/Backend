package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.TransportPlan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransportPlanRepository extends JpaRepository<TransportPlan, Long> {

    // 반정규화한 date_plan_id 기준. from/to가 끊긴 것도 함께 조회된다
    List<TransportPlan> findByDatePlanId(Long datePlanId);

    // trip 단위 조회용. 여러 DatePlan의 이동을 한번에 읽는다
    List<TransportPlan> findByDatePlanIdIn(List<Long> datePlanIds);

    // from/to가 끊긴 것도 함께 지운다 (date_plan_id 기준)
    @Modifying
    @Query("update TransportPlan tp set tp.isDeleted = true where tp.datePlanId = :datePlanId")
    void softDeleteAllByDatePlanId(@Param("datePlanId") Long datePlanId);

}
