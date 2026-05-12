package NoDam.Demo.place.domain;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.common.type.SeasonType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.type.WeatherType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long regionId; // cross-module: 최고 하위 region 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PlaceType placeType;

    @Column(nullable = false, unique = true)
    private String googleId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double x; // 경도

    @Column(nullable = false)
    private Double y; // 위도

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private WeatherType recommendWeatherType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private TripThemeType recommendTripThemeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private SeasonType recommendSeasonType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private PriceType priceType;

    @Builder
    public Place(Long regionId, PlaceType placeType, String googleId, String name, String address,
                 Double x, Double y, WeatherType recommendWeatherType,
                 TripThemeType recommendTripThemeType, SeasonType recommendSeasonType, PriceType priceType) {
        this.regionId = regionId;
        this.placeType = placeType;
        this.googleId = googleId;
        this.name = name;
        this.address = address;
        this.x = x;
        this.y = y;
        this.recommendWeatherType = recommendWeatherType;
        this.recommendTripThemeType = recommendTripThemeType;
        this.recommendSeasonType = recommendSeasonType;
        this.priceType = priceType;
    }
}
