package NoDam.Demo.plan.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.PlanStatus;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.request.DatePlanRequestDto;
import NoDam.Demo.plan.dto.request.PlacePlanRequestDto;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.repository.TripStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanCreateService {

    private final PlanRepository planRepository;
    private final DatePlanRepository datePlanRepository;
    private final TransportPlanRepository transportPlanRepository;
    private final TripStatusRepository tripStatusRepository;

    private final TransactionTemplate transactionTemplate;

    public Trip updateTripStatus(
            Trip trip,
            Boolean status
    ) {
        int affected = transactionTemplate.execute((s)-> {
            return tripStatusRepository.tryUpdateTripStatus(trip.getId(), !status, status);
        });

        if(affected != 1)
            throw new CustomException(ErrorCode.ALREADY_PROCESSING);

        trip.updatePlanning(status);
        return trip;
    }

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
                        .googleIds(dto.getNecessaryPlaces() != null
                                ? dto.getNecessaryPlaces().stream().map(p -> p.getGoogleId()).toList()
                                : List.of())
                        .hotelPlaceId(dto.getHotelPlaceId())
                        .airportPlaceId(dto.getAirportPlaceId())
                        .airportTime(dto.getAirportTime())
                        .build())
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

    public void forceUpdateTripStatus(Trip trip, Boolean status) {
        transactionTemplate.execute(s -> {
            tripStatusRepository.tryUpdateTripStatusForce(trip.getId(), status);
            return null;
        });
        trip.updatePlanning(status);
    }

    @Transactional
    public void updateDatePlanStatus(DatePlan datePlan, PlanStatus status) {
        datePlan.updatePlanStatus(status);
        datePlanRepository.save(datePlan);
    }

}
