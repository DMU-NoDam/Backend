package NoDam.Demo.trip.service;

import NoDam.Demo.memo.service.MemoService;
import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.dto.response.TripMemberInfo;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripMemberFacadeService {

    private final TripMemberService tripMemberService;
    private final UserService userService;
    private final TripFacadeService tripFacadeService; // 혼자인 OWNER가 나갈 때 여행 삭제로 위임하기 위함
    private final MemoService memoService; // memo domain : 나가는 사용자의 메모 정리
    private final TransactionTemplate transactionTemplate; // 나가기 + 메모 삭제 원자성 처리용

    private static final String UNKNOWN_USER_NAME = "(알 수 없음)";

    // 멤버 목록 조회는 해당 여행의 멤버만 가능
    public List<TripMemberInfo> getMembers(Long tripId, Long requesterUserId) {
        tripMemberService.requireRole(tripId, requesterUserId); // 멤버가 아니면 NOT_AUTHOR

        List<TripMember> members = tripMemberService.getMembers(tripId);

        List<Long> userIds = members.stream()
                .map(TripMember::getUserId)
                .toList();

        Map<Long, User> userMap = userService.getUsers(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        return members.stream()
                .map(member -> TripMemberInfo.from(member, userMap.get(member.getUserId())))
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

        // 나가기 처리와 본인 메모 삭제를 하나의 트랜잭션으로 묶는다
        transactionTemplate.execute(status -> {
            tripMemberService.leave(tripId, userId, newOwnerUserId);
            memoService.deleteAllByTripIdAndUserId(tripId, userId);
            return null;
        });
    }

}
