package NoDam.Demo.place.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    private Double lat;
    private Double lng;

    public static boolean isSameLocation(double lat1, double lon1, double lat2, double lon2) {
        return Math.abs(lat1 - lat2) <= 0.001 && Math.abs(lon1 - lon2) <= 0.001;
    }
}
