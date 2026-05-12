package NoDam.Demo.plan.domain;

import NoDam.Demo.trip.domain.Trip;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Column(nullable = true)
    private Integer takeTime; // 소요 시간 (분)

    @Column(nullable = true)
    private Long toPlaceId; // 출발지 place id (cross-module: place 참조)

    @Column(nullable = true)
    private Long fromPlaceId; // 도착지 place id (cross-module: place 참조)

    @Column(nullable = true)
    private String googleId; // 구글 길찾기 api 발급 id

    @Builder
    public TransportPlan(DatePlan trip, LocalTime startTime, LocalTime endTime,
                         Integer takeTime, Long toPlaceId, Long fromPlaceId, String googleId) {
        super(trip, startTime, endTime);
        this.takeTime = takeTime;
        this.toPlaceId = toPlaceId;
        this.fromPlaceId = fromPlaceId;
        this.googleId = googleId;
    }
}
