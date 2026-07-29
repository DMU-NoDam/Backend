package NoDam.Demo.trip.dto.response;

import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TripMemberInfo {
    private Long userId;
    private TripMemberRole role;
    private LocalDateTime joinedAt;

    public static TripMemberInfo from(TripMember tripMember) {
        return TripMemberInfo.builder()
                .userId(tripMember.getUserId())
                .role(tripMember.getRole())
                .joinedAt(tripMember.getJoinedAt())
                .build();
    }
}
