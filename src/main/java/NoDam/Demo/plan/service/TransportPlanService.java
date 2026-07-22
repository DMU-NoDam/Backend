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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportPlanService {

    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;
    private final DatePlanRepository datePlanRepository;

    private final Logger logger = LoggerFactory.getLogger(TransportPlanService.class);

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

    // 경로 계산 결과(RouteInfo)로 TransportPlan 하나를 조립·저장한다 (leg 단위로 즉시 저장하여 중간 실패 시 앞선 결과를 잃지 않는다)
    public TransportPlan saveTransportLeg(TransportLeg leg, RouteInfo routeInfo) {
        TransportPlan transportPlan = TransportPlan.builder()
                .fromPlacePlan(leg.from())
                .toPlacePlan(leg.to())
                .routeInfo(routeInfo)
                .build();

        return transportPlanRepository.save(transportPlan);
    }

    // 해당 날짜의 이동 일정 생성이 모두 끝났음을 표시한다. 비어있는 leg가 남아있으면 완료 처리하지 않는다
    public void completeTransportPlanning(DatePlan datePlan) {
        List<TransportLeg> emptyLegs = findEmptyTransportLegs(datePlan);
        if (!emptyLegs.isEmpty()) {
            logger.warn("transport plan is not completed datePlanId={}, emptyLegs={}", datePlan.getId(), emptyLegs.size());
            return;
        }

        datePlan.updatePlanStatus(PlanStatus.TRANSPORT_PLANNED);
        datePlanRepository.save(datePlan);
    }
}
