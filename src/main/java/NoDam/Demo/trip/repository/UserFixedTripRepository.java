package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.UserFixedTrip;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserFixedTripRepository extends JpaRepository<UserFixedTrip, UserFixedTrip.UserFixedTripId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserFixedTrip u WHERE u.userId = :userId AND u.date BETWEEN :startDate AND :endDate")
    List<UserFixedTrip> findAllByUserIdAndDateRangeForUpdate(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT u FROM UserFixedTrip u WHERE u.userId = :userId AND u.date BETWEEN :startDate AND :endDate")
    List<UserFixedTrip> findAllByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    void deleteByUserIdAndTrip(Long userId, Trip trip);
}
