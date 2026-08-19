package NoDam.Demo.plan.repository;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.domain.PlacePlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface PlacePlanRepository extends JpaRepository<PlacePlan, Long> {

    List<PlacePlan> findByDatePlanId(Long datePlanId);

    List<PlacePlan> findByDatePlanIdOrderByOrderIndexAsc(Long datePlanId);

    @Query("SELECT pp FROM PlacePlan pp JOIN FETCH pp.datePlan WHERE pp.id = :id")
    Optional<PlacePlan> findByIdWithDatePlan(@Param("id") Long id);

    // trip + theme 기준으로 배정된 place 조회 (공항, 호텔은 제외함)
    @Query("SELECT p FROM PlacePlan pp " +
           "JOIN Place p ON pp.placeId = p.id " +
           "JOIN pp.datePlan dp " +
           "WHERE dp.tripId = :tripId " +
           "AND dp.tripThemeType = :theme " +
           "AND (dp.airportPlaceId IS NULL OR pp.placeId <> dp.airportPlaceId) " +
           "AND (dp.hotelPlaceId IS NULL OR pp.placeId <> dp.hotelPlaceId)")
    List<Place> findPlacesByTripIdAndTheme(@Param("tripId") Long tripId, @Param("theme") TripThemeType theme);

    // PlacePlan에 배정된 Place 단건 조회 (cross-module: place 참조)
    @Query("SELECT p FROM Place p WHERE p.id = :placeId")
    Optional<Place> findPlaceById(@Param("placeId") Long placeId);

    @Modifying
    @Query("update PlacePlan pp set pp.isDeleted = true where pp.id = :id")
    void softDelete(@Param("id") Long PlacePlanId);

}
