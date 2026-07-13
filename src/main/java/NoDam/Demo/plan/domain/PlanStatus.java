package NoDam.Demo.plan.domain;

import java.util.List;

public enum PlanStatus {

    // DatePlan 생성됨 -> 다음 호출 : POST /trip/api/{tripId}/place-plans
    CREATED,

    // 공항·호텔 PlacePlan 생성 완료 (AI 일정 생성 중 실패해 중단된 상태)
    // -> 다음 호출 : POST /trip/api/{tripId}/place-plans (재호출, 고정 일정은 건너뛰고 AI 일정만 다시 생성)
    FIXED_PLANNED,

    // AI PlacePlan 저장 완료 -> 다음 호출 : POST /trip/api/{tripId}/transport-plans
    AI_PLANNED,

    // TransportPlan 생성 완료 -> 다음 호출 없음 (일정 완성, GET /plan/api/{tripId} 로 조회)
    TRANSPORT_PLANNED,

    // 완성 후 수정 중 -> 다음 호출 없음
    EDIT;

    // DatePlan이 아직 없는 경우(Trip 생성 직후)는 status 자체가 없으며, POST /trip/api/{tripId}/date-plans 를 호출해야 한다

    public boolean isBefore(PlanStatus other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isAfterOrEqual(PlanStatus other) {
        return this.ordinal() >= other.ordinal();
    }

    // 여러 DatePlan 중 가장 뒤처진 상태 = trip 전체의 진행 상태
    public static PlanStatus lowest(List<PlanStatus> statuses) {
        PlanStatus lowest = null;
        for (PlanStatus status : statuses) {
            if (lowest == null || status.isBefore(lowest)) lowest = status;
        }
        return lowest;
    }
}
