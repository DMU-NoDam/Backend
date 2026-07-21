package NoDam.Demo.plan.repository;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.domain.DatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DatePlanRepository extends JpaRepository<DatePlan, Long> {

    // DatePlan은 (tripId, date, theme) 단위로 존재하므로 theme까지 지정해야 단건이 보장된다
    Optional<DatePlan> findByTripIdAndDateAndTripThemeType(Long tripId, LocalDate date, TripThemeType tripThemeType);

    @Query("select distinct dp from DatePlan dp left join fetch dp.placePlans p where dp.tripId = :tripId")
    List<DatePlan> findAllDatePlanWithPlans(@Param("tripId") Long tripId);

    @Query("select distinct dp from DatePlan dp left join fetch dp.placePlans pp left join fetch pp.fromTransport left join fetch pp.toTransport where dp.tripId = :tripId")
    List<DatePlan> findAllDatePlanWithPlansWithTransport(@Param("tripId") Long tripId);

    @Query("select dp from DatePlan dp where dp.tripId in :tripIds")
    List<DatePlan> findAllByTripIdIn(@Param("tripIds") List<Long> tripIds);

}
