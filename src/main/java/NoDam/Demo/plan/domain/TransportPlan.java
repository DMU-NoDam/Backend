package NoDam.Demo.plan.domain;

import NoDam.Demo.common.converter.RouteInfoConverter;
import NoDam.Demo.plan.dto.response.RouteInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transport_plan")
@DiscriminatorValue("TRANSPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TransportPlan extends Plan {

    @Column(nullable = false)
    private Long toPlaceId; // 출발지 place id (cross-module: place 참조)

    @Column(nullable = false)
    private Long fromPlaceId; // 도착지 place id (cross-module: place 참조)

    @Column(nullable = false)
    private Integer totalDistanceMeters;

    @Column(nullable = false)
    private Integer totalDurationSeconds;

    @Convert(converter = RouteInfoConverter.class)
    @Column(nullable = true, columnDefinition = "JSON")
    private RouteInfo routeInfo;

    @Builder
    public TransportPlan(DatePlan datePlan, LocalTime beforePlanEndTime,
                         Long toPlaceId, Long fromPlaceId, RouteInfo routeInfo) {
        super(datePlan, beforePlanEndTime, calcEndTime(beforePlanEndTime, routeInfo));
        this.toPlaceId = toPlaceId;
        this.fromPlaceId = fromPlaceId;
        this.routeInfo = routeInfo;
        if (routeInfo != null) {
            this.totalDistanceMeters = routeInfo.getTotalDistanceMeters();
            this.totalDurationSeconds = routeInfo.getTotalDurationSeconds();
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
