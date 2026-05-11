package NoDam.Demo.trip.dto.request;

import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.common.type.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TripCreateDto {

    @NotBlank
    private String name;

    @NotBlank
    private String uuid;

    @NotNull
    private int personCount;

    // can null
    private ScheduleType scheduleType;

    // can null
    private PriceType priceType;

    @NotBlank
    private String startDate;

    @NotBlank
    private String endDate;

}
