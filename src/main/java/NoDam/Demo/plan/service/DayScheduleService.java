package NoDam.Demo.plan.service;

import NoDam.Demo.ai.AiBuildDayScheduleDto;
import NoDam.Demo.ai.AiService;
import NoDam.Demo.ai.Prompt;
import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.ai.AiRecommendPlaceResponseDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DayScheduleService {

    private final AiService aiService;

    @Value("${external.ai.provider}")
    private String aiProvider;

    private final Logger logger = LoggerFactory.getLogger(DayScheduleService.class);

    // step 3+4 통합: 필수 장소 배치 + 추천 장소에서 장소 선정
    public List<PlacePlanRequestDto> buildSchedule(
            ScheduleType scheduleType,
            TripThemeType themeType,
            List<PlaceInfo> necessaryPlaces,
            List<PlacePlanInfo> fixedPlans,
            Map<PlaceType, List<RecommendPlaceResult>> candidates,
            List<Place> previousDaysPlaces
    ) {
        if ("mock".equals(aiProvider)) return buildScheduleMock(candidates);

        AiBuildDayScheduleDto request = AiBuildDayScheduleDto.builder()
                .scheduleType(scheduleType)
                .themeType(themeType)
                .necessaryPlaces(necessaryPlaces.stream().map(AiBuildDayScheduleDto.PlaceItem::of).toList())
                .fixedPlans(fixedPlans.stream().map(AiBuildDayScheduleDto.FixedPlanItem::of).toList())
                .previousDaysPlaces(previousDaysPlaces.stream().map(AiBuildDayScheduleDto.PlaceItem::of).toList())
                .candidates(candidates)
                .build();

        AiRecommendPlaceResponseDto response = aiService.call(
                Prompt.BUILD_DAY_SCHEDULE, AiRecommendPlaceResponseDto.class, request);

        return response.getSelectedPlaces().stream()
                .map(s -> new PlacePlanRequestDto(
                        TimeUtil.toLocalTime(s.getStartTime()),
                        TimeUtil.toLocalTime(s.getEndTime()),
                        s.getPlaceId()
                ))
                .toList();
    }

    // mock: 식당 12:00~14:00, 관광지 15:00~16:00 각 1개
    private List<PlacePlanRequestDto> buildScheduleMock(Map<PlaceType, List<RecommendPlaceResult>> candidates) {
        List<PlacePlanRequestDto> result = new ArrayList<>();

        List<RecommendPlaceResult> restaurants = candidates.getOrDefault(PlaceType.RESTAURANT, List.of());
        if (!restaurants.isEmpty())
            result.add(new PlacePlanRequestDto(LocalTime.of(12, 0), LocalTime.of(14, 0), restaurants.get(0).getPlace().getId()));

        List<RecommendPlaceResult> sights = candidates.getOrDefault(PlaceType.SIGHT, List.of());
        if (!sights.isEmpty())
            result.add(new PlacePlanRequestDto(LocalTime.of(15, 0), LocalTime.of(16, 0), sights.get(0).getPlace().getId()));

        return result;
    }

}
