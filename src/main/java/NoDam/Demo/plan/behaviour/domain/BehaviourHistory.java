package NoDam.Demo.plan.behaviour.domain;

import NoDam.Demo.plan.behaviour.converter.BehaviourConverter;
import NoDam.Demo.common.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DatePlan에 적용된 behaviour 기록. (datePlanId, version) unique가 CAS 역할을 한다
@Entity
@Table(name = "behaviour_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_behaviour_history_date_plan_version",
                columnNames = {"date_plan_id", "version"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BehaviourHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_plan_id", nullable = false)
    private Long datePlanId;

    @Column(nullable = false)
    private long version;

    // rebase까지 끝나 실제로 적용된 형태
    @Convert(converter = BehaviourConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private Behaviour behaviour;

    @Builder
    public BehaviourHistory(Long datePlanId, long version, Behaviour behaviour) {
        this.datePlanId = datePlanId;
        this.version = version;
        this.behaviour = behaviour;
    }

}
