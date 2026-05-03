package NoDam.Demo.trip.dto.request;

import NoDam.Demo.common.type.PersonType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TransportType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
    private PersonType personType;

    @NotBlank
    private String startDate;

    @NotBlank
    private String endDate;

    private Long price;
}
