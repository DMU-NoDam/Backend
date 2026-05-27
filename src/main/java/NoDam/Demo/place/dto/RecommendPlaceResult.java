package NoDam.Demo.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendPlaceResult {

    private PlaceInfo place;
    private ScoreDetail scoreDetail;

    @Getter
    @AllArgsConstructor
    public static class ScoreDetail {
        private int price;
        private int season;
        private int theme;
        private int weather;
        private int total;
    }

}
