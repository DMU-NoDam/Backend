package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.PlacePlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlacePlanRepository extends JpaRepository<PlacePlan, Long> {

    List<PlacePlan> findByDatePlanId(Long datePlanId);

    @Query("SELECT pp FROM PlacePlan pp LEFT JOIN FETCH pp.departureTransport LEFT JOIN FETCH pp.arrivalTransport WHERE pp.datePlan.id = :datePlanId")
    List<PlacePlan> findByDatePlanIdWithTransport(@Param("datePlanId") Long datePlanId);

    @Query("SELECT pp FROM PlacePlan pp JOIN FETCH pp.datePlan LEFT JOIN FETCH pp.departureTransport LEFT JOIN FETCH pp.arrivalTransport WHERE pp.id = :id")
    Optional<PlacePlan> findByIdWithDatePlanAndTransport(@Param("id") Long id);
}
