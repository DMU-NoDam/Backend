package NoDam.Demo.trip.dto.response;

import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TripMemberInfo {
    private Long userId;
    private String userName;
    private TripMemberRole role;
    private LocalDateTime joinedAt;

    // user는 조회되지 않은 경우(삭제된 유저 등) null일 수 있으며, 이때 userName은 null
    public static TripMemberInfo from(TripMember tripMember, User user) {
        return TripMemberInfo.builder()
                .userId(tripMember.getUserId())
                .userName(user == null ? null : user.getName())
                .role(tripMember.getRole())
                .joinedAt(tripMember.getJoinedAt())
                .build();
    }
}
