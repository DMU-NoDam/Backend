package NoDam.Demo.plan.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.dto.request.DatePlanRequestDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlanRepository;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.repository.TripStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanCreateService {

    private final PlanRepository planRepository;
    private final DatePlanRepository datePlanRepository;
    private final TripStatusRepository tripStatusRepository;

    private final TransactionTemplate transactionTemplate;

    public void updateTripStatus(
            Trip trip,
            Boolean status
    ) {
        int affected = transactionTemplate.execute((s)-> {
            return tripStatusRepository.tryUpdateTripStatus(trip.getId(), !status, status);
        });

        if(affected != 1)
            throw new CustomException(ErrorCode.ALREADY_PROCESSING);
    }

    // date plans 를 생성하는 함수
    public List<DatePlan> createDatePlans(
            Trip trip,
            List<DatePlanRequestDto> datePlans
    ) {
        List<DatePlan> entities = datePlans.stream()
                .map(dto -> {
                    return DatePlan.builder()
                            .date(dto.getDate())
                            .trip(trip)
                            .regionId(dto.getRegionId())
                            .tripThemeType(dto.getThemeType())
                            .googleIds(List.of()) // todo
                            .build();
                })
                .toList();

        return datePlanRepository.saveAll(entities);
    }

    // date plan을 기준으로 plans를 생성하는 함수
    public DatePlan createPlans(
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

}
