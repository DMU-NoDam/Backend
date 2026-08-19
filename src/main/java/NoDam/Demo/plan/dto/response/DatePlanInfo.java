package NoDam.Demo.plan.dto.response;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.region.dto.response.RegionResponseDto;
import NoDam.Demo.trip.dto.response.TripInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatePlanInfo {

    private Long id;

    private LocalDate date;
    private TripInfoDto tripInfo;
    private TripThemeType datePlanTheme;

    private RegionResponseDto region;

    private Long version;
    private List<PlacePlanInfo> placePlanInfos;

}
