package NoDam.Demo.plan.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import NoDam.Demo.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanSelectService {

    private final DatePlanRepository datePlanRepository;
    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    public List<DatePlan> findAllDatePlan(Trip trip) {
        return datePlanRepository.findAllDatePlanWithPlans(trip.getId());
    }

    public List<DatePlan> findAllDatePlanWithTransport(Trip trip) {
        return datePlanRepository.findAllDatePlanWithPlansWithTransport(trip.getId());
    }

    public List<PlacePlan> findPlacePlansByDatePlan(DatePlan datePlan) {
        return placePlanRepository.findByDatePlanId(datePlan.getId());
    }

    public List<PlacePlan> findPlacePlansByDatePlanWithTransport(DatePlan datePlan) {
        return placePlanRepository.findByDatePlanIdWithTransport(datePlan.getId());
    }

    public PlacePlan findPlacePlanWithDatePlanAndTransport(Long placePlanId) {
        return placePlanRepository.findByIdWithDatePlanAndTransport(placePlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    // placeType에 해당하는 PlacePlan 중 placeId가 null인 것(placeholder) 반환
    public Optional<PlacePlan> findEmptyPlacePlanByType(DatePlan datePlan, PlaceType placeType) {
        return placePlanRepository.findByDatePlanIdAndPlaceType(datePlan.getId(), placeType)
                .stream()
                .filter(pp -> pp.getPlaceId() == null)
                .findFirst();
    }

    public boolean hasTransportPlan(DatePlan datePlan) {
        return !transportPlanRepository.findByFromPlacePlan_DatePlanId(datePlan.getId()).isEmpty();
    }

    public TransportPlan findTransportPlanById(Long transportPlanId) {
        return transportPlanRepository.findByIdWithDatePlan(transportPlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

}
