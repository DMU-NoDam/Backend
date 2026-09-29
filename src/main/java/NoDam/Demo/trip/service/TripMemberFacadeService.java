package NoDam.Demo.trip.service;

import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.dto.response.TripMemberInfo;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripMemberFacadeService {

    private final TripMemberService tripMemberService;
    private final TripFacadeService tripFacadeService; // 혼자인 OWNER가 나갈 때 여행 삭제로 위임하기 위함
    private final UserService userService; // user domain : 멤버 목록에 사용자 이름을 함께 보여주기 위함

    private static final String UNKNOWN_USER_NAME = "(알 수 없음)";

    // 멤버 목록 조회는 해당 여행의 멤버만 가능
    public List<TripMemberInfo> getMembers(Long tripId, Long requesterUserId) {
        tripMemberService.requireRole(tripId, requesterUserId); // 멤버가 아니면 NOT_AUTHOR

        List<TripMember> members = tripMemberService.getMembers(tripId);

        List<Long> userIds = members.stream().map(TripMember::getUserId).toList();
        Map<Long, String> userNameById = userService.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return members.stream()
                .map(m -> TripMemberInfo.from(m, userNameById.getOrDefault(m.getUserId(), UNKNOWN_USER_NAME)))
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
