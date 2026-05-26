package NoDam.Demo.plan.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.PlanStatus;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import NoDam.Demo.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Function;

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

    public Map<Trip, List<DatePlan>> findAllByTrip(List<Trip> trips) {
        List<Long> tripIds = trips.stream().map(Trip::getId).toList();
        Map<Long, Trip> tripById = trips.stream().collect(Collectors.toMap(Trip::getId, Function.identity()));
        return datePlanRepository.findAllByTripIdIn(tripIds).stream()
                .collect(Collectors.groupingBy(dp -> tripById.get(dp.getTripId())));
    }

    // todo : domain 로직이 db담당인 query service에 들어왔음, ddd나 domain service로 옮길 것!
    public Map<Trip, Boolean> getTripStatus(List<Trip> trips) {
        Map<Trip, List<DatePlan>> datePlans = findAllByTrip(trips);
        Map<Trip, Boolean> tripStatus = new HashMap<>();

        for(Trip eachTrip : datePlans.keySet()) {
            List<DatePlan> datePlan = datePlans.get(eachTrip);
            boolean status = !datePlan // 한개라도 ai planned가 아닌게 있다면 false
                    .stream()
                    .anyMatch(p->!p.getPlanStatus().equals(PlanStatus.AI_PLANNED));
            tripStatus.put(eachTrip, status);
        }

        return tripStatus;
    }

    public Boolean getTripStatus(Trip trip) {
        List<DatePlan> datePlans = findAllDatePlan(trip);
        boolean status = !datePlans // 한개라도 ai planned가 아닌게 있다면 false
                .stream()
                .anyMatch(p->!p.getPlanStatus().equals(PlanStatus.AI_PLANNED));

        return status;
    }

}
