package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.repository.TripMemberRepository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripMemberService {

    private final TripMemberRepository tripMemberRepository;
    private final TransactionTemplate transactionTemplate;

    public boolean isMember(Long tripId, Long userId) {
        return tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
    }

    public TripMemberRole requireRole(Long tripId, Long userId) {
        return tripMemberRepository.findByTripIdAndUserId(tripId, userId)
                .map(TripMember::getRole)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_AUTHOR));
    }

    // OWNER 권한이 필요한 동작(여행 삭제, 초대 발급 등)에서 호출
    public void requireOwner(Long tripId, Long userId) {
        if (requireRole(tripId, userId) != TripMemberRole.OWNER)
            throw new CustomException(ErrorCode.NOT_AUTHOR);
    }

    public List<TripMember> getMembers(Long tripId) {
        return tripMemberRepository.findAllByTripId(tripId);
    }

    public List<TripMember> getMyMemberships(Long userId) {
        return tripMemberRepository.findAllByUserId(userId);
    }

    // 여행 생성 직후 OWNER 등록, 초대 수락/링크 참여 시 MEMBER 등록에 공통 사용
    // 동시성 : unique(trip_id, user_id) 제약 기반, 이미 참여 중이면 예외로 알린다(멱등 반환 아님 - 사용자 요청에 따름)
    public TripMember addMember(Long tripId, Long userId, TripMemberRole role) {
        if (tripMemberRepository.existsByTripIdAndUserId(tripId, userId))
            throw new CustomException(ErrorCode.ALREADY_JOINED_TRIP);

        TripMember tripMember = TripMember.builder()
                .tripId(tripId)
                .userId(userId)
                .role(role)
                .build();

        try {
            return transactionTemplate.execute(status -> tripMemberRepository.save(tripMember));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 이미 참여 처리된 경우
            throw new CustomException(ErrorCode.ALREADY_JOINED_TRIP);
        }
    }

    // 나가기 : MEMBER는 바로 나가기. OWNER는 newOwnerUserId(위임 대상)가 반드시 필요하며,
    // 위임 후 기존 OWNER의 멤버십을 제거한다.
    // 혼자인 OWNER(위임할 대상이 없는 경우)는 이 메서드까지 오지 않는다 - facade에서 여행 삭제로 처리한다.
    public void leave(Long tripId, Long userId, Long newOwnerUserId) {
        TripMember tripMember = tripMemberRepository.findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_AUTHOR));

        if (!tripMember.isOwner()) {
            tripMemberRepository.delete(tripMember);
            return;
        }

        if (newOwnerUserId == null || newOwnerUserId.equals(userId))
            throw new CustomException(ErrorCode.BAD_REQUEST); // 위임 대상을 지정해야 함(본인 제외)

        TripMember newOwner = tripMemberRepository.findByTripIdAndUserId(tripId, newOwnerUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND)); // 위임 대상이 이 여행의 멤버가 아님

        transactionTemplate.execute(status -> {
            newOwner.promoteToOwner();
            tripMemberRepository.save(newOwner);
            tripMemberRepository.delete(tripMember);
            return null;
        });
    }

    public long countMembers(Long tripId) {
        return tripMemberRepository.countByTripId(tripId);
    }

    // 여행 삭제 시 멤버십 정리(트랜잭션 내부에서 호출)
    public void deleteAllByTripId(Long tripId) {
        tripMemberRepository.deleteAllByTripId(tripId);
    }

}
