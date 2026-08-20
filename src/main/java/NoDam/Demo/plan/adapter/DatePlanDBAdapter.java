package NoDam.Demo.plan.adapter;

import NoDam.Demo.plan.behaviour.adapter.BehaviourHistoryRepository;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import jakarta.persistence.EntityManager;
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
    private final BehaviourHistoryRepository behaviourHistoryRepository;

    private final EntityManager entityManager;

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
    @Transactional(readOnly = true)
    public List<DatePlan> datePlans(Long tripId) {
        List<DatePlan> datePlans = datePlanRepository.findAllDatePlanWithPlans(tripId);

        if (datePlans.isEmpty()) return datePlans;

        Map<Long, List<TransportPlan>> transportPlansByDatePlanId = new HashMap<>();
        for (DatePlan datePlan : datePlans)
            transportPlansByDatePlanId.put(datePlan.getId(), new ArrayList<>());

        for (TransportPlan transportPlan : transportPlanRepository.findByDatePlanIdIn(new ArrayList<>(transportPlansByDatePlanId.keySet())))
            transportPlansByDatePlanId.get(transportPlan.getDatePlanId()).add(transportPlan);

        for (DatePlan datePlan : datePlans)
            datePlan.loadTransportPlans(transportPlansByDatePlanId.get(datePlan.getId()));

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
        datePlanRepository.save(datePlan);
    }

    @Override
    @Transactional
    public void deleteDatePlan(DatePlan datePlan) {
        Long datePlanId = datePlan.getId();

        transportPlanRepository.softDeleteAllByDatePlanId(datePlanId);
        placePlanRepository.softDeleteAllByDatePlanId(datePlanId);
        behaviourHistoryRepository.deleteAllByDatePlanId(datePlanId);

        entityManager.flush();
        entityManager.clear(); 

        datePlanRepository.deleteById(datePlanId);
    }

    @Override
    @Transactional
    public void deleteDatePlans(List<DatePlan> datePlans) {
        for (DatePlan datePlan : datePlans)
            deleteDatePlan(datePlan);
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
