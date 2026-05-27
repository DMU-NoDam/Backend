package NoDam.Demo.place;

import NoDam.Demo.common.type.*;
import NoDam.Demo.place.domain.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("select p from Place p where p.googleId = :googleId")
    Optional<Place> findByGoogleId(@Param("googleId") String googleId);

    @Query("select p from Place p where p.googleId in :googleIds")
    List<Place> findAllByGoogleId(@Param("googleIds") List<String> googleIds);

    @Query("SELECT p FROM Place p WHERE p.placeType = :placeType AND p.regionId = :regionId")
    List<Place> findByPlaceTypeAndRegionId(@Param("placeType") PlaceType placeType, @Param("regionId") Long regionId, Pageable pageable);

//    @Query("SELECT p FROM Place p " +
//            "WHERE p.placeType = :placeType " +
//            "AND p.regionId = :regionId " +
//            "AND (:priceType IS NULL OR p.priceType = :priceType or p.priceType is null) " +
//            "AND (:season IS NULL OR p.recommendSeasonType = :season or p.recommendSeasonType is null) " +
//            "AND (:theme IS NULL OR p.recommendTripThemeType = :theme or p.recommendTripThemeType is null) " +
//            "AND (:weather IS NULL OR p.recommendWeatherType = :weather or p.recommendWeatherType is null) " +
//            "AND (:#{#excludeIds.isEmpty()} = true OR p.id NOT IN :excludeIds)")
//    List<Place> findPlacesByFilters(
//            @Param("placeType") PlaceType placeType,
//            @Param("regionId") Long regionId,
//            @Param("priceType") PriceType priceType,
//            @Param("season") SeasonType season,
//            @Param("theme") TripThemeType theme,
//            @Param("weather") WeatherType weather,
//            @Param("excludeIds") List<Long> excludeIds,
//            Pageable pageable
//    );

    @Query(value =
        "WITH scored AS (" +
        "  SELECT id, region_id, place_type, google_id, name, address, lon, lat," +
        "         recommend_weather_type, recommend_trip_theme_type, recommend_season_type, price_type," +
        "         is_deleted, created_at, updated_at," +
        "         CASE WHEN :priceType IS NOT NULL AND price_type              = :priceType THEN 1 ELSE 0 END AS price_weight," +
        "         CASE WHEN :season    IS NOT NULL AND recommend_season_type   = :season    THEN 1 ELSE 0 END AS season_weight," +
        "         CASE WHEN :theme     IS NOT NULL AND recommend_trip_theme_type = :theme   THEN 1 ELSE 0 END AS theme_weight," +
        "         CASE WHEN :weather   IS NOT NULL AND recommend_weather_type  = :weather   THEN 1 ELSE 0 END AS weather_weight" +
        "  FROM place" +
        "  WHERE place_type = :placeType" +
        "  AND region_id = :regionId" +
        "  AND id NOT IN (:excludeIds)" +
        ") " +
        "SELECT id, region_id, place_type, google_id, name, address, lon, lat," +
        "       recommend_weather_type, recommend_trip_theme_type, recommend_season_type, price_type," +
        "       is_deleted, created_at, updated_at" +
        " FROM scored" +
        " ORDER BY (price_weight + season_weight + theme_weight + weather_weight) DESC",
        nativeQuery = true)
    List<Place> findPlacesByFilters(
            @Param("placeType") String placeType,
            @Param("regionId") Long regionId,
            @Param("priceType") String priceType,
            @Param("season") String season,
            @Param("theme") String theme,
            @Param("weather") String weather,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable
    );

}
