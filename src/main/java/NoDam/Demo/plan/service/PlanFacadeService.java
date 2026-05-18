package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanFacadeService {

    private final PlanSelectService planSelectService;
    private final PlanCreateService planCreateService;
    private final PlanDeleteService planDeleteService;
    private final AutoCreatePlanService autoCreatePlanService;
    private final TripSelectService tripSelectService;

    public List<DatePlan> getPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        return planSelectService.findAllDatePlanWithTransport(trip);
    }

    public void deletePlacePlan(Long placePlanId, Long userId) {
        PlacePlan placePlan = planSelectService.findPlacePlanWithDatePlanAndTransport(placePlanId);
        tripSelectService.findById(placePlan.getDatePlan().getTripId(), userId);

        planDeleteService.deletePlacePlanWithTransports(placePlan);
    }

    public PlacePlanInfo changePlacePlan(Long oldPlacePlanId, Long newPlaceId, Long userId) {
        PlacePlan oldPlacePlan = planSelectService.findPlacePlanWithDatePlanAndTransport(oldPlacePlanId);
        DatePlan datePlan = oldPlacePlan.getDatePlan();
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);

        planDeleteService.deletePlacePlanWithTransports(oldPlacePlan);

        PlacePlan newPlacePlan = planCreateService.createPlacePlan(
                datePlan,
                newPlaceId,
                oldPlacePlan.getStartTime(),
                oldPlacePlan.getEndTime()
        );

        autoCreatePlanService.createAllTransportPlan(trip, datePlan);

        return PlacePlanInfo.of(newPlacePlan);
    }

}
