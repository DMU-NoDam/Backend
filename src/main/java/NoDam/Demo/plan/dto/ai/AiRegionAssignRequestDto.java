package NoDam.Demo.plan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRegionAssignRequestDto {

    private List<String> dates;                  // yyyy-MM-dd
    private List<RegionInfo> regions;
    private List<PlaceCoordinate> necessaryPlaces;
    private PlaceCoordinate airport;             // nullable
    private PlaceCoordinate hotel;               // nullable

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionInfo {
        private Long regionId;
        private String name;
        private Double lat;
        private Double lon;
        private int placeCount; // 해당 region의 필수 장소 수
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceCoordinate {
        private Long placeId;
        private String name;
        private Double lat;
        private Double lon;
    }

}
