package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.trip.domain.Trip;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacePlanRepository extends JpaRepository<PlacePlan, Long> {
    List<PlacePlan> findByDatePlanId(Long datePlanId);
}
