package NoDam.Demo.plan.repository;

import NoDam.Demo.plan.domain.TransportPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransportPlanRepository extends JpaRepository<TransportPlan, Long> {
    List<TransportPlan> findByFromPlacePlan_DatePlanId(Long datePlanId);

    @Query("SELECT t FROM TransportPlan t JOIN FETCH t.fromPlacePlan fp JOIN FETCH fp.datePlan WHERE t.id = :id")
    Optional<TransportPlan> findByIdWithDatePlan(@Param("id") Long id);

    @Modifying
    @Query("update TransportPlan t set t.isDeleted = true where t.id = :id")
    void softDelete(@Param("id") Long transportId);

    @Modifying
    @Query("update TransportPlan tp set tp.isDeleted = true " +
            "where tp.toPlacePlan.id = :placePlanId or tp.fromPlacePlan.id = :placePlanId")
    void softDeleteAllByPlacePlan(@Param("placePlanId") Long placePlanId);

    // TransportPlanRepository
    @Modifying
    @Query("UPDATE TransportPlan t SET t.fromPlacePlan = null WHERE t.fromPlacePlan.id = :id")
    void detachFromPlaceById(@Param("id") Long placeId);

    @Modifying
    @Query("UPDATE TransportPlan t SET t.toPlacePlan = null WHERE t.toPlacePlan.id = :id")
    void detachToPlaceById(@Param("id") Long placeId);

    @Modifying
    @Query("UPDATE TransportPlan t SET t.isDeleted = true WHERE t.fromPlacePlan.id = :id OR t.toPlacePlan.id = :id")
    void softDeleteByPlaceId(@Param("id") Long placeId);

}
