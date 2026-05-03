package NoDam.Demo.trip.domain;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.type.PersonType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TransportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "trip")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE trip SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Trip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String name;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(nullable = false)
    private Long siteId;

    @Column(nullable = false)
    private int personCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private ScheduleType scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private PersonType personType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private TransportType transportType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private Long price;

    @Builder
    public Trip(String name, Long userId, String uuid, Long siteId, int personCount,
                ScheduleType scheduleType, PersonType personType, TransportType transportType,
                LocalDate startDate, LocalDate endDate, Long price) {
        this.name = name;
        this.userId = userId;
        this.uuid = uuid;
        this.siteId = siteId != null ? siteId : 1L;
        this.personCount = personCount;
        this.scheduleType = scheduleType;
        this.personType = personType;
        this.transportType = transportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }
}
