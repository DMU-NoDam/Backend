package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.TransportPlan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportPlanRepository extends JpaRepository<TransportPlan, Long> {
    List<TransportPlan> findByFromPlacePlan_DatePlanId(Long datePlanId);
}
