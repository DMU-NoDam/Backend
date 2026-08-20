package NoDam.Demo.trip.repository;

import NoDam.Demo.trip.domain.TripMember;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    Optional<TripMember> findByTripIdAndUserId(Long tripId, Long userId);

    boolean existsByTripIdAndUserId(Long tripId, Long userId);

    // 여행 상세 화면의 멤버 목록 조회용 (tripId 기준, unique 인덱스가 커버)
    List<TripMember> findAllByTripId(Long tripId);

    // "내가 속한 여행" 목록 조회용 (userId 기준)
    List<TripMember> findAllByUserId(Long userId);

    // 여행 내 멤버 수 확인 (OWNER 나가기 정책 판단용)
    long countByTripId(Long tripId);

    void deleteByTripIdAndUserId(Long tripId, Long userId);

    void deleteAllByTripId(Long tripId);

}
