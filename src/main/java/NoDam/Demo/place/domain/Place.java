package NoDam.Demo.place.domain;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.type.PlaceStatus;
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

    @Column(nullable = true)
    private Long crawlId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PlaceStatus status;

    @Column(nullable = false)
    private Long regionId; // cross-module: 최고 하위 region 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 15)
    private PlaceType placeType;

    @Column(nullable = false, unique = true) // todo : 수정
    private String googleId;

    @Column(nullable = false)
    private String name; // google name_ko

    // nullable: place는 두 소스(crawl export=en/jp 있음, 앱 lazy 생성=단일 name만)로 만들어짐.
    @Column(nullable = true)
    private String nameEn; // google name_en (앱 lazy 생성 시 null)

    @Column(nullable = true)
    private String nameJp; // google name_jp (앱 lazy 생성 시 null)

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double lon; // x

    @Column(nullable = false)
    private Double lat; // y

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

    @Column(nullable = true)
    private Integer time; // 평균 소요 시간(hours)

    @Column(nullable = true)
    private Double score; // 통합 유명도 점수(대표)

    @Column(nullable = true)
    private Double scoreTop3; // 통합 유명도 점수(상위3)

    @Column(nullable = true, length = 500)
    private String summary; // 크롤 LLM이 생성한 장소 요약 (앱 lazy 생성 시 null)

    @Builder
    public Place(PlaceStatus status, Long regionId, PlaceType placeType, String googleId, String name,
                 String nameEn, String nameJp, String address, Double lon, Double lat,
                 WeatherType recommendWeatherType, TripThemeType recommendTripThemeType,
                 SeasonType recommendSeasonType, PriceType priceType,
                 Integer time, Double score, Double scoreTop3, String summary) {
        this.status = status;
        this.regionId = regionId;
        this.placeType = placeType;
        this.googleId = googleId;
        this.name = name;
        this.nameEn = nameEn;
        this.nameJp = nameJp;
        this.address = address;
        this.lon = lon;
        this.lat = lat;
        this.recommendWeatherType = recommendWeatherType;
        this.recommendTripThemeType = recommendTripThemeType;
        this.recommendSeasonType = recommendSeasonType;
        this.priceType = priceType;
        this.time = time;
        this.score = score;
        this.scoreTop3 = scoreTop3;
        this.summary = summary;
    }
}
