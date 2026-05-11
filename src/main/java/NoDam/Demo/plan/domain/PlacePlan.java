package NoDam.Demo.plan.domain;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.trip.domain.Trip;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private Long placeId; //제 cross-module: place 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PlaceType placeType; // 반정규화

    @Builder
    public PlacePlan(Trip trip, LocalDateTime startTime, LocalDateTime endTime,
                     Long placeId, PlaceType placeType) {
        super(trip, startTime, endTime);
        this.placeId = placeId;
        this.placeType = placeType;
    }
}
