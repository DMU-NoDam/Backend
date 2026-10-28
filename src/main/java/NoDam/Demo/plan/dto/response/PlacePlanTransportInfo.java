package NoDam.Demo.plan.dto.response;

import NoDam.Demo.plan.domain.TransportPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PlacePlan 기준 앞뒤 이동. 앞뒤 장소가 없거나 계산 전이면 null
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacePlanTransportInfo {

    private Long placePlanId;
    private TransportPlanInfo previousTransport; // before -> target
    private TransportPlanInfo nextTransport;     // target -> after

    public static PlacePlanTransportInfo of(Long placePlanId, TransportPlan previous, TransportPlan next) {
        return PlacePlanTransportInfo.builder()
                .placePlanId(placePlanId)
                .previousTransport(previous == null ? null : TransportPlanInfo.of(previous))
                .nextTransport(next == null ? null : TransportPlanInfo.of(next))
                .build();
    }
}
