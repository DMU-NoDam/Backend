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
@Table(name = "place_plan")
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

    @OneToOne(mappedBy = "fromPlacePlan", orphanRemoval = true)
    private TransportPlan fromTransport;

    @OneToOne(mappedBy = "toPlacePlan", orphanRemoval = true)
    private TransportPlan toTransport;

    public void updatePlaceId(Long placeId) {
        this.placeId = placeId;
    }

    @Builder
    public PlacePlan(DatePlan datePlan, LocalTime startTime, LocalTime endTime, Long placeId) {
        super(startTime, endTime);
        this.datePlan = datePlan;
        this.placeId = placeId;
    }

    public void setFromTransportNull() { fromTransport = null; }
    public void setToTransportNull() { toTransport = null; }
}
