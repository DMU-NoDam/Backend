package NoDam.Demo.plan.dto.ai;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDayScheduleRequestDto {

    private ScheduleType scheduleType;
    private TripThemeType themeType;
    private List<PlaceInfo> necessaryPlaces;        // 이 날 반드시 방문할 장소
    private List<PlacePlanInfo> fixedPlans;         // 고정된 시간대 (공항, 호텔) + 장소 정보
    private List<PlaceInfo> previousDaysPlaces;     // 이전 날짜에 이미 선정된 장소 목록
    private Map<PlaceType, List<PlaceInfo>> candidates;

}
