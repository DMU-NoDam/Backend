package NoDam.Demo.trip.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveTripRequestDto {

    // OWNER가 나갈 때만 필요 (위임할 대상). MEMBER가 나갈 때는 무시된다.
    private Long newOwnerUserId;

}
