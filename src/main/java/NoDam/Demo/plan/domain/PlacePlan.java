package NoDam.Demo.plan.domain;

import jakarta.persistence.*;

import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "place_plan",
        indexes = @Index(name = "idx_place_plan_date_plan_order", columnList = "date_plan_id, order_index"))
@DiscriminatorValue("PLACE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE place_plan SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
public class PlacePlan extends Plan {

    @ManyToOne
    @JoinColumn(name = "date_plan_id")
    private DatePlan datePlan;

    @Column(nullable = true)
    private Long placeId; // cross-module: place 참조

    // TransportPlan은 fromPlacePlanId / toPlacePlanId로만 이 PlacePlan을 가리킨다 (역방향 연관 없음)

    @Column(nullable = false)
    /*
        mysql signed big int == java long (0 ~ 922 0000 0000 0000 0000)
        1 0000 0000 0000 0000 값 부터 시작 (, 2 0000 0000 0000 0000, 3 0000 0000 0000 0000, ... ~ 922)
        사이 삽입은 중간값으로 넣는다 : a + (b - a) / 2 (한 구간당 약 53번 분할 가능)
     */
    private Long orderIndex;

    public void updatePlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public void updateOrderIndex(Long orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Builder
    public PlacePlan(DatePlan datePlan, LocalTime startTime, LocalTime endTime, Long placeId, Long orderIndex) {
        super(startTime, endTime);
        this.datePlan = datePlan;
        this.placeId = placeId;
        this.orderIndex = orderIndex;
    }
}
