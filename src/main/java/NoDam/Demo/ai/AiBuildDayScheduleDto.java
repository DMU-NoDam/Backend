package NoDam.Demo.ai;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBuildDayScheduleDto {

    private ScheduleType scheduleType;
    private TripThemeType themeType;
    private List<PlaceItem> necessaryPlaces;
    private List<FixedPlanItem> fixedPlans;
    private Map<PlaceType, List<RecommendPlaceResult>> candidates;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceItem {

        private Long id;
        private PlaceType placeType;
        private String name;
        private Double lon;
        private Double lat;

        public static PlaceItem of(PlaceInfo info) {
            return PlaceItem.builder()
                    .id(info.getId())
                    .placeType(info.getPlaceType())
                    .name(info.getName())
                    .lon(info.getLon())
                    .lat(info.getLat())
                    .build();
        }

        public static PlaceItem of(Place place) {
            return PlaceItem.builder()
                    .id(place.getId())
                    .placeType(place.getPlaceType())
                    .name(place.getName())
                    .lon(place.getLon())
                    .lat(place.getLat())
                    .build();
        }

        public static PlaceItem empty() {
            return PlaceItem.builder()
                    .name("empty place")
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedPlanItem {

        private LocalTime startTime;
        private LocalTime endTime;
        private PlaceItem place;

        public static FixedPlanItem of(PlacePlanInfo info) {
            return FixedPlanItem.builder()
                    .startTime(info.getStartTime())
                    .endTime(info.getEndTime())
                    .place(info.getPlaceInfo() != null ? PlaceItem.of(info.getPlaceInfo()) : PlaceItem.empty())
                    .build();
        }
    }
}
