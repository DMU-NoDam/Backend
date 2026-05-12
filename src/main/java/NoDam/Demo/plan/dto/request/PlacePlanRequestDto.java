package NoDam.Demo.plan.dto.request;

import NoDam.Demo.common.type.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacePlanRequestDto {

    private LocalTime startTime; // todo : 30분 단위
    private LocalTime endTime; // todo : 30분 단위

    private Long placeId;

}
