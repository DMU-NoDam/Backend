package NoDam.Demo.plan.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FixPlacePlanRequestDto {

    private Long placePlanId;
    private Boolean isFixed;

}
