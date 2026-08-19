package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanDeleteService {

    private final PlacePlanRepository placePlanRepository;
    private final DatePlanRepository datePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    private final EntityManager entityManager;

    @Transactional
    public void deleteDatePlansWithTransports(List<DatePlan> datePlans) {
        // todo : publish plan deleted event!
    }

    @Transactional
    public void deletePlacePlanWithTransports(Long placePlanId) {
        // todo : publish plan deleted event!
    }

}
