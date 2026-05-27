package NoDam.Demo.place.domain;

import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.common.type.SeasonType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.type.WeatherType;
import NoDam.Demo.place.dto.RecommendPlaceResult;

public enum TypeWeight {

    PRICE(10),
    SEASON(5),
    THEME(5),
    WEATHER(3);

    private final int weight;

    TypeWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public static RecommendPlaceResult.ScoreDetail computeDetail(
            Place place,
            PriceType priceType,
            SeasonType season,
            TripThemeType theme,
            WeatherType weather
    ) {
        int p = (priceType != null && priceType == place.getPriceType())             ? PRICE.weight  : 0;
        int s = (season    != null && season    == place.getRecommendSeasonType())   ? SEASON.weight : 0;
        int t = (theme     != null && theme     == place.getRecommendTripThemeType())? THEME.weight  : 0;
        int w = (weather   != null && weather   == place.getRecommendWeatherType())  ? WEATHER.weight: 0;
        return new RecommendPlaceResult.ScoreDetail(p, s, t, w, p + s + t + w);
    }

}

