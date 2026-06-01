package NoDam.Demo.common.type;

public enum PlanStatus {

    CREATED,           // DatePlan 생성됨
    FIXED_PLANNED,     // 공항·호텔 PlacePlan 생성 완료
    AI_PLANNED,        // AI PlacePlan 저장 완료
    TRANSPORT_PLANNED; // TransportPlan 생성 완료

    public boolean isBefore(PlanStatus other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isAfterOrEqual(PlanStatus other) {
        return this.ordinal() >= other.ordinal();
    }
}
