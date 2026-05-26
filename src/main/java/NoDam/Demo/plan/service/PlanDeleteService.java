package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanDeleteService {

    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    @Transactional
    public void deletePlacePlanWithTransports(PlacePlan placePlan) {
        if (placePlan.getDepartureTransport() != null)
            transportPlanRepository.deleteById(placePlan.getDepartureTransport().getId());
        if (placePlan.getArrivalTransport() != null)
            transportPlanRepository.deleteById(placePlan.getArrivalTransport().getId());
        placePlanRepository.deleteById(placePlan.getId());
    }

}
