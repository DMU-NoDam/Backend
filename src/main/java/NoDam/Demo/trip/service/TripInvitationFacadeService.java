package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripInvitation;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.dto.response.TripInvitationPreviewInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripInvitationFacadeService {

    private final TripInvitationService tripInvitationService;
    private final TripMemberService tripMemberService;
    private final TripSelectService tripSelectService;

    // 초대 링크 발급/재조회 : 여행당 하나만 유지되며 여러 명이 반복 사용 가능. OWNER만 발급 가능
    public TripInvitation getOrCreateLink(Long tripId, Long ownerUserId) {
        tripMemberService.requireOwner(tripId, ownerUserId);
        return tripInvitationService.getOrCreateLinkInvitation(tripId, ownerUserId);
    }

    // 링크 미리보기 (비로그인) : 여행 기본 정보만 노출
    public TripInvitationPreviewInfo previewByToken(String token) {
        TripInvitation invitation = tripInvitationService.findByToken(token);
        Trip trip = tripSelectService.findByIdPublic(invitation.getTripId());
        return TripInvitationPreviewInfo.from(trip);
    }

    // 링크 참여 (로그인 필요) : 수락 절차 없이 즉시 멤버로 추가. 이미 멤버라면 에러 없이 성공 처리(멱등)
    public void joinByToken(String token, Long userId) {
        TripInvitation invitation = tripInvitationService.findByToken(token);

        try {
            tripMemberService.addMember(invitation.getTripId(), userId, TripMemberRole.MEMBER);
        } catch (CustomException e) {
            if (e.errorCode != ErrorCode.ALREADY_JOINED_TRIP)
                throw e;
            // 이미 참여 중인 경우는 멱등하게 성공 처리
        }
    }

}
