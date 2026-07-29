package NoDam.Demo.trip.service;

import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.dto.response.TripMemberInfo;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripMemberFacadeService {

    private final TripMemberService tripMemberService;
    private final TripFacadeService tripFacadeService; // 혼자인 OWNER가 나갈 때 여행 삭제로 위임하기 위함

    // 멤버 목록 조회는 해당 여행의 멤버만 가능
    public List<TripMemberInfo> getMembers(Long tripId, Long requesterUserId) {
        tripMemberService.requireRole(tripId, requesterUserId); // 멤버가 아니면 NOT_AUTHOR

        return tripMemberService.getMembers(tripId).stream()
                .map(TripMemberInfo::from)
                .toList();
    }

    // 나가기 : MEMBER는 바로 나가기.
    // OWNER는 다른 멤버가 있으면 newOwnerUserId로 위임 후 나가고, 혼자면(위임 대상 없음) 여행 삭제로 처리한다.
    public void leave(Long tripId, Long userId, Long newOwnerUserId) {
        TripMemberRole role = tripMemberService.requireRole(tripId, userId);

        if (role == TripMemberRole.OWNER && tripMemberService.countMembers(tripId) <= 1) {
            tripFacadeService.deleteTrip(userId, tripId);
            return;
        }

        tripMemberService.leave(tripId, userId, newOwnerUserId);
    }

}
