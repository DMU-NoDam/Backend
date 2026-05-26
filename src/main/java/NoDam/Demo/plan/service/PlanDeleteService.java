package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanDeleteService {

    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    private final EntityManager entityManager;

    @Transactional
    public void deletePlacePlanWithTransports(PlacePlan placePlan) {
        long placePlanId = placePlan.getId();

        if(placePlan.getToTransport() != null)
            transportPlanRepository.softDelete(placePlan.getToTransport().getId());
        if(placePlan.getFromTransport() != null)
            transportPlanRepository.softDelete(placePlan.getFromTransport().getId());

        placePlanRepository.softDelete(placePlanId);
    }

}
