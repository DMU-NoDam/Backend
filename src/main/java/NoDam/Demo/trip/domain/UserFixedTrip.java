package NoDam.Demo.trip.domain;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_fixed_trip")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(UserFixedTrip.UserFixedTripId.class)
public class UserFixedTrip {

    @Id
    private Long userId;

    @Id
    private LocalDate date;

    @JoinColumn(nullable = false, name = "trip_id")
    @ManyToOne(targetEntity = Trip.class)
    private Trip trip;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserFixedTripId implements Serializable {
        private Long userId;
        private LocalDate date;
    }
}
