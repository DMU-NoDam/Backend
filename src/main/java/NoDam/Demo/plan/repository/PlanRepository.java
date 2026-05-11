package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
