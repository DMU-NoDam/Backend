package NoDam.Demo.plan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_plan")
@DiscriminatorValue("PLACE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PlacePlan extends Plan {

    @ManyToOne
    @JoinColumn(name = "date_plan_id", nullable = false)
    private DatePlan datePlan;

    @Column(nullable = false)
    private Long placeId; // cross-module: place 참조

    @OneToOne(mappedBy = "fromPlacePlan")
    private TransportPlan departureTransport;

    @OneToOne(mappedBy = "toPlacePlan")
    private TransportPlan arrivalTransport;

    @Builder
    public PlacePlan(DatePlan datePlan, LocalTime startTime, LocalTime endTime, Long placeId) {
        super(startTime, endTime);
        this.datePlan = datePlan;
        this.placeId = placeId;
    }
}
