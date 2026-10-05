package NoDam.Demo.memo.repository;

import NoDam.Demo.memo.domain.Memo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    List<Memo> findAllByTripIdAndUserIdOrderByIdDesc(Long tripId, Long userId); // 최신순

    void deleteAllByTripId(Long tripId); // 여행 삭제 시 모든 사용자의 메모 정리용

    void deleteAllByTripIdAndUserId(Long tripId, Long userId); // 여행 나가기 시 본인 메모 정리용

}
