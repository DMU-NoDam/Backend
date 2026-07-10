package NoDam.Demo.plan.dto;

import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;

// 두 PlacePlan 사이의 이동 구간. transportPlan == null 이면 아직 이동이 없음(비어있음)을 뜻한다
public record TransportLeg(PlacePlan from, PlacePlan to, TransportPlan transportPlan) {
}
