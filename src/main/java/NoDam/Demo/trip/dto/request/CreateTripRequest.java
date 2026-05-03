package NoDam.Demo.trip.dto.request;

import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TransportType;
import NoDam.Demo.common.type.TripThemeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTripRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String uuid;

    @NotNull
    private int personCount;

    private String site;

    private ScheduleType scheduleType;
    private TransportType transportType;
    private TripThemeType tripThemeType;

    @NotBlank
    private String startDate;

    @NotBlank
    private String endDate;

    private Long price;
}
