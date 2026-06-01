package NoDam.Demo.plan.domain;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.type.PlanStatus;
import NoDam.Demo.common.type.TripThemeType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "date_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE date_plan SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class DatePlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(nullable = false)
    private Long regionId; // cross-module: region 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TripThemeType tripThemeType;

    @Column(name = "google_id_list", nullable = true)
    private String googleIds;

    @Column(nullable = true)
    private Long hotelPlaceId; // 해당 날짜 숙소 place id

    @Column(nullable = true)
    private Long airportPlaceId; // 해당 날짜 공항 place id (첫날 = 목적지 도착 공항, 마지막날 = 목적지 출발 공항)

    @Column(nullable = true)
    private LocalTime airportTime; // 첫날 = 한국 출발 시간 ceiling, 마지막날 = 목적지 출발 시간 ceiling

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus planStatus;

    @OneToMany(mappedBy = "datePlan")
    private List<PlacePlan> placePlans = new ArrayList<>();

    @Builder
    public DatePlan(LocalDate date, Long tripId, Long regionId, List<String> googleIds, TripThemeType tripThemeType,
                    Long hotelPlaceId, Long airportPlaceId, LocalTime airportTime) {
        this.date = date;
        this.tripId = tripId;
        this.regionId = regionId;
        this.googleIds = googleIds != null ? String.join(",", googleIds) : "";
        this.tripThemeType = tripThemeType;
        this.hotelPlaceId = hotelPlaceId;
        this.airportPlaceId = airportPlaceId;
        this.airportTime = airportTime;
        this.planStatus = PlanStatus.CREATED;
    }

    public void updatePlanStatus(PlanStatus planStatus) {
        this.planStatus = planStatus;
    }
}
