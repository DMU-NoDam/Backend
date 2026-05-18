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

    @Query("SELECT p FROM Place p " +
            "WHERE p.placeType = :placeType " +
            "AND p.regionId = :regionId " +
            "AND (:priceType IS NULL OR p.priceType = :priceType or p.priceType is null) " +
            "AND (:season IS NULL OR p.recommendSeasonType = :season or p.recommendSeasonType is null) " +
            "AND (:theme IS NULL OR p.recommendTripThemeType = :theme or p.recommendTripThemeType is null) " +
            "AND (:weather IS NULL OR p.recommendWeatherType = :weather or p.recommendWeatherType is null) " +
            "AND (:#{#excludeIds.isEmpty()} = true OR p.id NOT IN :excludeIds)")
    List<Place> findPlacesByFilters(
            @Param("placeType") PlaceType placeType,
            @Param("regionId") Long regionId,
            @Param("priceType") PriceType priceType,
            @Param("season") SeasonType season,
            @Param("theme") TripThemeType theme,
            @Param("weather") WeatherType weather,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable
    );

}
