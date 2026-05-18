package NoDam.Demo.plan.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePlacePlanRequestDto {

    private Long oldPlacePlanId;
    private Long newPlaceId;

}
