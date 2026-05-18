package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanFacadeService {

    private final PlanSelectService planSelectService;
    private final TripSelectService tripSelectService;

    public List<DatePlan> getPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        return planSelectService.findAllDatePlanWithTransport(trip);
    }

}
