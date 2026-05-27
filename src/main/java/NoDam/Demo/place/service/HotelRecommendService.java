package NoDam.Demo.place.service;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.common.type.PriceType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.place.dto.RecommendPlaceResult;
import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HotelRecommendService {

    private final PlaceSelectService placeSelectService;

    // TODO: 외부 API 호출 fallback 구현
    public Optional<PlaceInfo> recommend(Region region, PriceType priceType, List<Long> excludeIds) {
        List<RecommendPlaceResult> hotels = placeSelectService.recommendPlaces(
                PlaceType.HOTEL, region, priceType, null, null, null, excludeIds, 1);
        return hotels.isEmpty() ? Optional.empty() : Optional.of(hotels.get(0).getPlace());
    }

}
