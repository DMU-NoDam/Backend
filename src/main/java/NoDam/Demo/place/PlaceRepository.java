package NoDam.Demo.place;

import NoDam.Demo.common.type.*;
import NoDam.Demo.place.domain.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p " +
            "WHERE p.placeType = :placeType " +
            "AND p.regionId = :regionId " +
            "AND (:priceType IS NULL OR p.priceType = :priceType) " +
            "AND (:season IS NULL OR p.recommendSeasonType = :season) " +
            "AND (:theme IS NULL OR p.recommendTripThemeType = :theme) " +
            "AND (:weather IS NULL OR p.recommendWeatherType = :weather)")
    List<Place> findPlacesByFilters(
            @Param("placeType") PlaceType placeType,
            @Param("regionId") Long regionId,
            @Param("priceType") PriceType priceType,
            @Param("season") SeasonType season,
            @Param("theme") TripThemeType theme,
            @Param("weather") WeatherType weather,
            Pageable pageable
    );

}
