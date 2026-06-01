package NoDam.Demo.plan.service;

import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanDeleteService {

    private final PlacePlanRepository placePlanRepository;
    private final DatePlanRepository datePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    private final EntityManager entityManager;

    @Transactional
    public void deleteDatePlansWithTransports(List<DatePlan> datePlans) {
        List<PlacePlan> allPlacePlans = datePlans.stream()
                .flatMap(datePlan -> datePlan.getPlacePlans().stream())
                .toList();

        placePlanRepository.deleteAll(allPlacePlans); // delete all place plan, transport plans (by removal)
        datePlanRepository.deleteAll(datePlans);
    }

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
