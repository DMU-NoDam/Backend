package NoDam.Demo.plan.domain;

import NoDam.Demo.common.converter.RouteInfoConverter;
import NoDam.Demo.plan.dto.response.RouteInfo;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "transport_plan", indexes = @Index(name = "idx_transport_plan_date_plan", columnList = "date_plan_id"))
@DiscriminatorValue("TRANSPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE transport_plan SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
public class TransportPlan extends Plan {

    // 반정규화 : from/to가 끊겨도 어느 날짜의 이동이었는지 남는다 (date plan 단위 조회용)
    @Column(name = "date_plan_id", nullable = false)
    private Long datePlanId;

    // 연관관계 대신 id만 가진다 (merge 시 JPA가 PlacePlan <-> TransportPlan 그래프를 타고 들어가지 않게 한다)
    @Column(name = "from_place_plan_id", nullable = true)
    private Long fromPlacePlanId;

    @Column(name = "to_place_plan_id", nullable = true)
    private Long toPlacePlanId;

    @Column(nullable = false)
    private Integer totalDistanceMeters;

    @Column(nullable = false)
    private Integer takeTime; // 초 단위 (Google API 반환값), endTime은 시간 단위 올림 처리

    @Convert(converter = RouteInfoConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private RouteInfo routeInfo;

    @Builder
    public TransportPlan(PlacePlan fromPlacePlan, PlacePlan toPlacePlan, RouteInfo routeInfo) {
        super(fromPlacePlan.getEndTime(), calcEndTime(fromPlacePlan.getEndTime(), routeInfo));
        this.datePlanId = fromPlacePlan.getDatePlan().getId();
        this.fromPlacePlanId = fromPlacePlan.getId();
        this.toPlacePlanId = toPlacePlan.getId();
        this.routeInfo = routeInfo;
        if (routeInfo != null) {
            this.totalDistanceMeters = routeInfo.getTotalDistanceMeters();
            this.takeTime = routeInfo.getTotalDurationSeconds();
        }
    }

    // 한쪽만 끊어도 쓸 수 없는 이동이므로 양 끝을 함께 끊는다
    public void detach() {
        this.fromPlacePlanId = null;
        this.toPlacePlanId = null;
    }

    public boolean getDetached() {
        return fromPlacePlanId == null || toPlacePlanId == null;
    }

    // 소요 시간 더한 뒤 1시간 단위 올림
    private static LocalTime calcEndTime(LocalTime start, RouteInfo routeInfo) {
        if(start == null) return null; // todo : place plan end time can null 허용으로 인한 transport plan time 모두 can null
        if (routeInfo == null || routeInfo.getTotalDurationSeconds() == null) return start;
        LocalTime end = start.plusSeconds(routeInfo.getTotalDurationSeconds());
        if (end.getMinute() == 0 && end.getSecond() == 0) return end;
        return end.truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

}
