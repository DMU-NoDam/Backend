package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TripStatusRepository extends JpaRepository<Trip, Long> {

    @Modifying
    @Query("UPDATE Trip t SET t.isPlanning = :updateStatus WHERE t.id = :id AND t.isPlanning = :oldStatus AND (t.isDeleted = false)")
    int tryUpdateTripStatus(
            @Param("id") Long id,
            @Param("oldStatus") Boolean oldStatus,
            @Param("updateStatus") Boolean updateStatus
    );

    @Modifying
    @Query("UPDATE Trip t SET t.isPlanning = :updateStatus WHERE t.id = :id AND (t.isDeleted = false)")
    void tryUpdateTripStatusForce(
            @Param("id") Long id,
            @Param("updateStatus") Boolean updateStatus
    );

}
