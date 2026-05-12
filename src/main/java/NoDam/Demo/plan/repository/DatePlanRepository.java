package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DatePlanRepository extends JpaRepository<DatePlan, Long> {

    @Query("select distinct dp from DatePlan dp left join fetch dp.plans p where dp.trip.id = :tripId")
    List<DatePlan> findAllDatePlanWithPlans(@Param("tripId") Long tripId);

}
