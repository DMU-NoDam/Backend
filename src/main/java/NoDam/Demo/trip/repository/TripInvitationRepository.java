package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.TripInvitation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TripInvitationRepository extends JpaRepository<TripInvitation, Long> {

    Optional<TripInvitation> findByToken(String token);

    // 여행당 하나의 재사용 가능한 링크를 유지하기 위한 조회
    Optional<TripInvitation> findByTripId(Long tripId);

    void deleteAllByTripId(Long tripId);

}
