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

    @Column(nullable = false)
    private Long placeId; //제 cross-module: place 참조

    @Builder
    public PlacePlan(DatePlan datePlan, LocalTime startTime, LocalTime endTime,
                     Long placeId) {
        super(datePlan, startTime, endTime);
        this.placeId = placeId;
    }
}
