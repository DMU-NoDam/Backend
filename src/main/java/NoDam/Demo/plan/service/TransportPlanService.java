package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.TransportLeg;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransportPlanService {

    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;
    private final DatePlanRepository datePlanRepository;

    // 이동이 아직 없는 구간(leg)을 찾는다. transportPlan은 null(비어있음)로 채워 반환한다
    public List<TransportLeg> findEmptyTransportLegs(DatePlan datePlan) {
        List<PlacePlan> placePlans = placePlanRepository.findByDatePlanIdWithTransport(datePlan.getId())
                .stream()
                .sorted(Comparator.comparing(PlacePlan::getStartTime))
                .toList();

        List<TransportLeg> legs = new ArrayList<>();
        for (int i = 0; i < placePlans.size() - 1; i++) {
            PlacePlan current = placePlans.get(i);
            PlacePlan next = placePlans.get(i + 1);

            if (current.getFromTransport() != null) continue;
            if (current.getPlaceId() == null || next.getPlaceId() == null) continue;

            legs.add(new TransportLeg(current, next, null));
        }
        return legs;
    }

    // 경로 계산 결과(RouteInfo)로 TransportPlan을 조립·저장한다
    public List<TransportPlan> saveTransportLegs(DatePlan datePlan, Map<TransportLeg, RouteInfo> results) {
        List<TransportPlan> transportPlans = results.entrySet().stream()
                .map(entry -> TransportPlan.builder()
                        .fromPlacePlan(entry.getKey().from())
                        .toPlacePlan(entry.getKey().to())
                        .routeInfo(entry.getValue())
                        .build())
                .toList();
        List<TransportPlan> saved =  transportPlanRepository.saveAll(transportPlans);
        datePlan.updatePlanStatus(PlanStatus.TRANSPORT_PLANNED);
        datePlanRepository.save(datePlan);

        return saved;
    }
}
