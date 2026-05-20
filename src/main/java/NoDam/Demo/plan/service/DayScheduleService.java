package NoDam.Demo.plan.service;

import NoDam.Demo.ai.AiService;
import NoDam.Demo.ai.Prompt;
import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.plan.dto.ai.AiDayScheduleRequestDto;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.ai.AiRecommendPlaceResponseDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DayScheduleService {

    private final AiService aiService;

    private final Logger logger = LoggerFactory.getLogger(DayScheduleService.class);

    // step 3+4 통합: 필수 장소 배치 + 추천 장소 채움을 AI 1회 호출로 처리
    public List<PlacePlanRequestDto> buildSchedule(
            ScheduleType scheduleType,
            TripThemeType themeType,
            List<PlaceInfo> necessaryPlaces,
            List<PlacePlanInfo> fixedPlans,
            Map<PlaceType, List<PlaceInfo>> candidates,
            List<Place> previousDaysPlaces
    ) {
        AiDayScheduleRequestDto request = AiDayScheduleRequestDto.builder()
                .scheduleType(scheduleType)
                .themeType(themeType)
                .necessaryPlaces(necessaryPlaces)
                .fixedPlans(fixedPlans)
                .previousDaysPlaces(previousDaysPlaces.stream().map(PlaceInfo::of).toList())
                .candidates(candidates)
                .build();

        AiRecommendPlaceResponseDto response = aiService.call(
                Prompt.BUILD_DAY_SCHEDULE, AiRecommendPlaceResponseDto.class, request);

        if (response == null || response.getSelectedPlaces() == null || response.getSelectedPlaces().isEmpty()) {
            logger.warn("DayScheduleService.buildSchedule :: AI 응답 없음, fallback 적용");
            return fallback(necessaryPlaces);
        }

        return response.getSelectedPlaces().stream()
                .map(s -> new PlacePlanRequestDto(
                        TimeUtil.toLocalTime(s.getStartTime()),
                        TimeUtil.toLocalTime(s.getEndTime()),
                        s.getPlaceId()
                ))
                .toList();
    }

    // fallback: 필수 장소만 09:00부터 순서대로 배치 (장소당 2시간)
    private List<PlacePlanRequestDto> fallback(List<PlaceInfo> necessaryPlaces) {
        List<PlacePlanRequestDto> result = new ArrayList<>();
        LocalTime cursor = LocalTime.of(9, 0);
        for (PlaceInfo place : necessaryPlaces) {
            LocalTime end = cursor.plusHours(2);
            result.add(new PlacePlanRequestDto(cursor, end, place.getId()));
            cursor = end;
        }
        return result;
    }

}
