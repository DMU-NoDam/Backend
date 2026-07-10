package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.TripRequest;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {

    Optional<TripRequest> findByTripId(Long tripId);

    void deleteByTripId(Long tripId);

}
