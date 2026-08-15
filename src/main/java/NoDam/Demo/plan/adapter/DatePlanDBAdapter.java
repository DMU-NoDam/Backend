package NoDam.Demo.plan.adapter;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class DatePlanDBAdapter implements DatePlanDBPort {

    private final DatePlanRepository datePlanRepository;
    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    // transportPlans는 @Transient라 여기서 채워야 한다
    @Override
    public Optional<DatePlan> latestDatePlan(Long datePlanId) {
        Optional<DatePlan> datePlan = datePlanRepository.findByIdWithPlacePlans(datePlanId);

        if(datePlan.isEmpty())
            return Optional.empty();

        datePlan.get().loadTransportPlans(transportPlanRepository.findByDatePlanId(datePlanId));

        return datePlan;
    }

    @Override
    @Transactional
    public List<DatePlan> datePlans(Long tripId) {
        List<DatePlan> datePlans = datePlanRepository.findAllDatePlanWithPlansWithTransport(tripId);

        for(DatePlan each : datePlans) {
            each.getTransportPlans(); // left join해서 jpa 1차 캐시 안애 있음
        }

        return datePlans;
    }

    // 저장된 것 중 목록에서 빠진 것은 삭제하고, 목록에 있는 것은 저장한다
    @Override
    public void saveDatePlan(DatePlan datePlan) {
        List<PlacePlan> savedPlacePlans = placePlanRepository.findByDatePlanIdOrderByOrderIndexAsc(datePlan.getId());

        for (PlacePlan savedPlacePlan : savedPlacePlans)
            if (!contains(datePlan.getPlacePlans(), savedPlacePlan.getId()))
                placePlanRepository.delete(savedPlacePlan); // @SQLDelete, soft delete

        saveTransportPlans(datePlan);
        placePlanRepository.saveAll(datePlan.getPlacePlans());
    }

    // 끊긴 이동은 지우고 나머지는 저장한다. 채워지지 않았으면(@Transient) 손대지 않는다
    private void saveTransportPlans(DatePlan datePlan) {
        if (datePlan.getTransportPlans() == null) return;

        List<TransportPlan> detachedTransportPlans = new ArrayList<>();
        List<TransportPlan> connectedTransportPlans = new ArrayList<>();

        for (TransportPlan transportPlan : datePlan.getTransportPlans())
            if (transportPlan.getDetached()) detachedTransportPlans.add(transportPlan);
            else connectedTransportPlans.add(transportPlan);

        transportPlanRepository.deleteAll(detachedTransportPlans); // @SQLDelete, soft delete
        transportPlanRepository.saveAll(connectedTransportPlans);
    }

    private boolean contains(List<PlacePlan> placePlans, Long placePlanId) {
        for (PlacePlan placePlan : placePlans)
            if (Objects.equals(placePlan.getId(), placePlanId)) return true;

        return false;
    }

}
