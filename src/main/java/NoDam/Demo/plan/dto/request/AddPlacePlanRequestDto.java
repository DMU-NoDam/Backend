package NoDam.Demo.plan.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddPlacePlanRequestDto {

    private Long placeId;

    private Long previousPlacePlanId; // 맨 앞이면 null
    private Long nextPlacePlanId; // 맨 뒤면 null

}
