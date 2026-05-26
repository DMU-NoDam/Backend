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
@Table(name = "transport_plan")
@DiscriminatorValue("TRANSPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE transport_plan SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
public class TransportPlan extends Plan {

    @OneToOne
    @JoinColumn(name = "from_place_plan_id", nullable = false)
    private PlacePlan fromPlacePlan;

    @OneToOne
    @JoinColumn(name = "to_place_plan_id", nullable = false)
    private PlacePlan toPlacePlan;

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
        this.fromPlacePlan = fromPlacePlan;
        this.toPlacePlan = toPlacePlan;
        this.routeInfo = routeInfo;
        if (routeInfo != null) {
            this.totalDistanceMeters = routeInfo.getTotalDistanceMeters();
            this.takeTime = routeInfo.getTotalDurationSeconds();
        }
    }

    // 소요 시간 더한 뒤 1시간 단위 올림
    private static LocalTime calcEndTime(LocalTime start, RouteInfo routeInfo) {
        if (routeInfo == null || routeInfo.getTotalDurationSeconds() == null) return start;
        LocalTime end = start.plusSeconds(routeInfo.getTotalDurationSeconds());
        if (end.getMinute() == 0 && end.getSecond() == 0) return end;
        return end.truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

}
