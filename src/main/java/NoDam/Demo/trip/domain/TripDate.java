package NoDam.Demo.trip.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trip_date")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TripDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private Long regionId; // cross-module: region 참조

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "trip_date_google_id", joinColumns = @JoinColumn(name = "trip_date_id"))
    @Column(name = "google_id")
    private List<String> googleIds = new ArrayList<>();

    @Builder
    public TripDate(LocalDate date, Trip trip, Long regionId, List<String> googleIds) {
        this.date = date;
        this.trip = trip;
        this.regionId = regionId;
        this.googleIds = googleIds != null ? googleIds : new ArrayList<>();
    }
}
