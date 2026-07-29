package NoDam.Demo.trip.dto.response;

import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.trip.domain.Trip;
import lombok.Builder;
import lombok.Getter;

// 초대 링크 미리보기 (비로그인 접근 가능) - 여행 기본 정보만 노출한다
@Getter
@Builder
public class TripInvitationPreviewInfo {
    private String tripName;
    private String startDate;
    private String endDate;

    public static TripInvitationPreviewInfo from(Trip trip) {
        return TripInvitationPreviewInfo.builder()
                .tripName(trip.getName())
                .startDate(DateUtil.fromLocalDate(trip.getStartDate()))
                .endDate(DateUtil.fromLocalDate(trip.getEndDate()))
                .build();
    }
}
