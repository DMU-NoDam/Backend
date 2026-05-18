package NoDam.Demo.plan.domain;

import NoDam.Demo.common.type.TripThemeType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "date_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DatePlan {

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
    @Column(nullable = true, length = 15)
    private TripThemeType tripThemeType;

    @Column(name = "google_id_list", nullable = true)
    private String googleIds;

    @OneToMany(mappedBy = "datePlan")
    private List<PlacePlan> placePlans = new ArrayList<>();

    @Builder
    public DatePlan(LocalDate date, Long tripId, Long regionId, List<String> googleIds, TripThemeType tripThemeType) {
        this.date = date;
        this.tripId = tripId;
        this.regionId = regionId;
        this.googleIds = googleIds != null ? String.join(",", googleIds) : "";
        this.tripThemeType = tripThemeType;
    }
}
