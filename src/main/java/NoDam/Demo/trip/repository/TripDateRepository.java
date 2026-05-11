package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripDateRepository extends JpaRepository<TripDate, Long> {

    List<TripDate> findAllByTrip(Trip trip);

    boolean existsByTrip(Trip trip);
}
