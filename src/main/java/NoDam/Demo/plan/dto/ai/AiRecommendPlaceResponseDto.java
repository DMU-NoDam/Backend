package NoDam.Demo.plan.dto.ai;

import NoDam.Demo.adapter.ai.AiExample;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendPlaceResponseDto implements AiExample {

    private List<SelectedPlace> selectedPlaces;

    @Override
    public String toJsonStr() {
        return """
                {
                  "selectedPlaces": [
                    {
                      "placeId": 1,
                      "startTime": "09:00",
                      "endTime": "10:30"
                    }
                  ]
                }
                """;
    }

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
