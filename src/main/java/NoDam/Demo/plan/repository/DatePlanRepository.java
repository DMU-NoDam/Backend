package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.DatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DatePlanRepository extends JpaRepository<DatePlan, Long> {

    @Query("select distinct dp from DatePlan dp left join fetch dp.placePlans p where dp.tripId = :tripId")
    List<DatePlan> findAllDatePlanWithPlans(@Param("tripId") Long tripId);

    @Query("select distinct dp from DatePlan dp left join fetch dp.placePlans pp left join fetch pp.departureTransport left join fetch pp.arrivalTransport where dp.tripId = :tripId")
    List<DatePlan> findAllDatePlanWithPlansWithTransport(@Param("tripId") Long tripId);

}
