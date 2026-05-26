package NoDam.Demo.trip.dto.response;

import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.trip.domain.Trip;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripInfoDto {
    private Long id;
    private String name;
    private int personCount;
    private ScheduleType scheduleType;
    private TripThemeType tripThemeType; // can null, if null -> user not selected trip theme
    private PriceType priceType;
    private String startDate;
    private String endDate;
    private boolean isFixed;
    private Boolean planCreated;

    public static TripInfoDto from(Trip trip, boolean planStatus) {
        return TripInfoDto.builder()
                .id(trip.getId())
                .name(trip.getName())
                .personCount(trip.getPersonCount())
                .scheduleType(trip.getScheduleType())
                .tripThemeType(trip.getTripThemeType())
                .priceType(trip.getPriceType())
                .startDate(DateUtil.fromLocalDate(trip.getStartDate()))
                .endDate(DateUtil.fromLocalDate(trip.getEndDate()))
                .isFixed(trip.isFixed())
                .planCreated(planStatus)
                .build();
    }
}
