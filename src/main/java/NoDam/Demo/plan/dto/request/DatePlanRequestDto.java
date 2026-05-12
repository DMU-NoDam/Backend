package NoDam.Demo.plan.dto.request;

import NoDam.Demo.common.type.TripThemeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DatePlanRequestDto {

    private LocalDate date; // yyyy-mm-dd
    private Long regionId;
    private TripThemeType themeType;
    private String googleIds; // todo : 정규화?

}
