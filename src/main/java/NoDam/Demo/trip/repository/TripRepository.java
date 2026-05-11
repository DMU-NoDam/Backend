package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.Trip;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findByUuid(String uuid);

    List<Trip> findAllByUserId(Long userId);

}
