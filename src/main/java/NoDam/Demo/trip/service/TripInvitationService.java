package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.TripInvitation;
import NoDam.Demo.trip.repository.TripInvitationRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripInvitationService {

    private final TripInvitationRepository tripInvitationRepository;

    // 여행당 하나의 재사용 가능한 링크를 유지한다 (이미 있으면 그대로 반환)
    public TripInvitation getOrCreateLinkInvitation(Long tripId, Long inviterUserId) {
        return tripInvitationRepository.findByTripId(tripId)
                .orElseGet(() -> tripInvitationRepository.save(
                        TripInvitation.builder()
                                .tripId(tripId)
                                .inviterUserId(inviterUserId)
                                .token(UUID.randomUUID().toString())
                                .build()
                ));
    }

    public TripInvitation findByToken(String token) {
        return tripInvitationRepository.findByToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    // 여행 삭제 시 초대 데이터 정리(트랜잭션 내부에서 호출)
    public void deleteAllByTripId(Long tripId) {
        tripInvitationRepository.deleteAllByTripId(tripId);
    }

}
