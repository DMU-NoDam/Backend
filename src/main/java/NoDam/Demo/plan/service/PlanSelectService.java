package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PlanSelectService {

    private final DatePlanRepository datePlanRepository;

    private final DatePlanDBPort datePlanDBPort;

    // DatePlan이 하나도 없는 trip도 빈 list를 value로 가진다 (key 누락 방지)
    public Map<Trip, List<DatePlan>> findAllByTrip(List<Trip> trips) {
        List<Long> tripIds = trips.stream().map(Trip::getId).toList();
        Map<Long, Trip> tripById = trips.stream().collect(Collectors.toMap(Trip::getId, Function.identity()));

        Map<Trip, List<DatePlan>> datePlansByTrip = new HashMap<>();
        for (Trip trip : trips)
            datePlansByTrip.put(trip, new ArrayList<>());

        for (DatePlan datePlan : datePlanRepository.findAllByTripIdIn(tripIds))
            datePlansByTrip.get(tripById.get(datePlan.getTripId())).add(datePlan);

        return datePlansByTrip;
    }

    // todo : domain 로직이 db담당인 query service에 들어왔음, ddd나 domain service로 옮길 것!
    public Map<Trip, Boolean> getTripStatus(List<Trip> trips) {
        Map<Trip, List<DatePlan>> datePlans = findAllByTrip(trips);
        Map<Trip, Boolean> tripStatus = new HashMap<>();

        for(Trip eachTrip : datePlans.keySet()) {
            List<DatePlan> datePlan = datePlans.get(eachTrip);

            boolean status = !datePlan.isEmpty(); // DatePlan이 없으면 아직 일정 생성 전이므로 false
            for (DatePlan each : datePlan) { // 한개라도 ai planned가 아닌게 있다면 false
                if (!each.getPlanStatus().isAfterOrEqual(PlanStatus.AI_PLANNED)) {
                    status = false;
                    break;
                }
            }

            tripStatus.put(eachTrip, status);
        }

        return tripStatus;
    }

    public Boolean getTripStatus(Trip trip) {
        List<DatePlan> datePlans = datePlanDBPort.datePlans(trip.getId());

        boolean status = !datePlans.isEmpty(); // DatePlan이 없으면 아직 일정 생성 전이므로 false
        for (DatePlan each : datePlans) { // 한개라도 ai planned가 아닌게 있다면 false
            if (!each.getPlanStatus().isAfterOrEqual(PlanStatus.AI_PLANNED)) {
                status = false;
                break;
            }
        }

        return status;
    }

}
