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
public class AiRegionAssignResponseDto implements AiExample {

    private List<DateRegionMapping> assignments;

    @Override
    public String toJsonStr() {
        return """
                {
                  "assignments": [
                    {
                      "date": "2024-01-15",
                      "regionId": 1
                    }
                  ]
                }
                """;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRegionMapping {
        private String date;     // yyyy-MM-dd
        private Long regionId;
    }

}
