package NoDam.Demo.plan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendPlaceResponseDto {

    private List<SelectedPlace> selectedPlaces;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedPlace {

        private Long placeId;
        private String startTime; // HH:mm
        private String endTime;   // HH:mm
    }
}
