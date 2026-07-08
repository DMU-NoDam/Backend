package NoDam.Demo.plan.dto.request;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.region.domain.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DatePlanRequestDto {

    private LocalDate date; // yyyy-mm-dd
    private Region region; // not null
    private TripThemeType themeType; // not null
    private List<Place> necessaryPlaces; // not null
    private Place hotelPlace; // not null (추천 하고 들어올 것)
    private Place airportPlace; // nullable
    private LocalTime airportTime; // nullable

}
