package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.request.DatePlanRequestDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import NoDam.Demo.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanCreateService {

    private final PlanRepository planRepository;
    private final DatePlanRepository datePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    // date plans 를 생성하는 함수
    public List<DatePlan> createDatePlans(
            Trip trip,
            List<DatePlanRequestDto> datePlans
    ) {
        List<DatePlan> entities = datePlans.stream()
                .map(dto -> DatePlan.builder()
                        .date(dto.getDate())
                        .tripId(trip.getId())
                        .regionId(dto.getRegion().getId())
                        .tripThemeType(dto.getThemeType())
                        .necessaryPlaces(dto.getNecessaryPlaces() != null
                                ? dto.getNecessaryPlaces().stream().map(p -> p.getId()).toList()
                                : List.of())
                        .hotelPlaceId(dto.getHotelPlace().getId())
                        .airportPlaceId(dto.getAirportPlace().getId())
                        .airportTime(dto.getAirportTime())
                        .build())
                .toList();

        return datePlanRepository.saveAll(entities);
    }

    // date plan을 기준으로 plans를 생성하는 함수
    private DatePlan createPlans(
            DatePlan datePlan,
            List<PlacePlanRequestDto> plans
    ) {
        List<PlacePlan> entities = plans.stream()
                .map(dto -> PlacePlan.builder()
                        .datePlan(datePlan)
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .placeId(dto.getPlaceId())
                        .build())
                .toList();

        planRepository.saveAll(entities);
        return datePlanRepository.findById(datePlan.getId()).get();
    }

    // 공항(첫날/마지막날), 호텔(저녁·아침) 고정 PlacePlan 생성 + 저장, 상태를 FIXED_PLANNED로 전이
    public DatePlan createFixedPlans(Trip trip, DatePlan datePlan) {
        List<PlacePlanRequestDto> plans = new ArrayList<>();

        // 첫날: 도착 공항 (자정 ~ 공항 도착 시간)
        if (trip.getStartDate().equals(datePlan.getDate()))
            plans.add(new PlacePlanRequestDto(LocalTime.MIDNIGHT, datePlan.getAirportTime(), datePlan.getAirportPlaceId()));

        // 마지막날: 출발 공항 (공항 출발 시간 ~ 23:59)
        if (trip.getEndDate().equals(datePlan.getDate()))
            plans.add(new PlacePlanRequestDto(datePlan.getAirportTime(), LocalTime.of(23, 59), datePlan.getAirportPlaceId()));

        // 첫날 제외: 아침 첫 장소 = 전날 밤 잔 호텔 (자정 ~ 09:00)
        if (!trip.getStartDate().equals(datePlan.getDate())) {
            Long previousHotelPlaceId = datePlanRepository
                    .findByTripIdAndDateAndTripThemeType(
                            datePlan.getTripId(), datePlan.getDate().minusDays(1), datePlan.getTripThemeType())
                    .map(DatePlan::getHotelPlaceId)
                    .orElse(null);
            if (previousHotelPlaceId != null)
                plans.add(new PlacePlanRequestDto(LocalTime.MIDNIGHT, LocalTime.of(9, 0), previousHotelPlaceId));
        }

        // 마지막 날 제외: 저녁 호텔 (not null → 실제 호텔, null → placeholder)
        if (!trip.getEndDate().equals(datePlan.getDate()))
            plans.add(new PlacePlanRequestDto(LocalTime.of(20, 0), LocalTime.of(23, 50), datePlan.getHotelPlaceId()));

        DatePlan result = createPlans(datePlan, plans);
        datePlan.updatePlanStatus(PlanStatus.FIXED_PLANNED);
        datePlanRepository.save(datePlan);
        return result;
    }

    // AI 일정 PlacePlan 생성 + 저장, 상태를 AI_PLANNED로 전이
    public DatePlan createPlacePlans(DatePlan datePlan, List<PlacePlanRequestDto> plans) {
        DatePlan result = createPlans(datePlan, plans);
        datePlan.updatePlanStatus(PlanStatus.AI_PLANNED);
        datePlanRepository.save(datePlan);
        return result;
    }

    public List<TransportPlan> createTransportPlans(List<TransportPlan> transportPlans) {
        return transportPlanRepository.saveAll(transportPlans);
    }

    @Transactional
    public PlacePlan createPlacePlan(DatePlan datePlan, Long placeId, LocalTime startTime, LocalTime endTime) {
        PlacePlan entity = PlacePlan.builder()
                .datePlan(datePlan)
                .startTime(startTime)
                .endTime(endTime)
                .placeId(placeId)
                .build();
        return planRepository.save(entity);
    }

}
