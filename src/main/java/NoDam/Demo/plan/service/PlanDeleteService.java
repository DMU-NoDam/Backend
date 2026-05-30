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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanDeleteService {

    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    private final EntityManager entityManager;

    @Transactional
    public void deletePlacePlanWithTransports(Long placePlanId) {
        PlacePlan target = placePlanRepository.findById(placePlanId).orElseThrow();

        if (target.getToTransport() != null && target.getToTransport().getFromPlacePlan() != null)
            target.getToTransport().getFromPlacePlan().setFromTransportNull();

        if (target.getFromTransport() != null && target.getFromTransport().getToPlacePlan() != null)
            target.getFromTransport().getToPlacePlan().setToTransportNull();

        placePlanRepository.delete(target);
    }

}
