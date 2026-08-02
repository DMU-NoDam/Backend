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

    @Query("SELECT p FROM Place p" +
           " WHERE p.placeType = :placeType" +
           " AND p.regionId = :regionId" +
           " AND p.id NOT IN :excludeIds" +
           " ORDER BY (" +
           "     CASE WHEN p.priceType              = :priceType THEN :#{T(NoDam.Demo.place.domain.TypeWeight).PRICE.getWeight()}   ELSE 0 END" +
           "   + CASE WHEN p.recommendSeasonType    = :season    THEN :#{T(NoDam.Demo.place.domain.TypeWeight).SEASON.getWeight()}  ELSE 0 END" +
           "   + CASE WHEN p.recommendTripThemeType = :theme     THEN :#{T(NoDam.Demo.place.domain.TypeWeight).THEME.getWeight()}   ELSE 0 END" +
           "   + CASE WHEN p.recommendWeatherType   = :weather   THEN :#{T(NoDam.Demo.place.domain.TypeWeight).WEATHER.getWeight()} ELSE 0 END" +
           " ) DESC, p.score DESC")
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
