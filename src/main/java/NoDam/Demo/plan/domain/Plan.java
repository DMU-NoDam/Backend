package NoDam.Demo.plan.domain;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.trip.domain.Trip;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "plan")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "plan_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE plan SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public abstract class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    protected Plan(Trip trip, LocalDateTime startTime, LocalDateTime endTime) {
        this.trip = trip;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
