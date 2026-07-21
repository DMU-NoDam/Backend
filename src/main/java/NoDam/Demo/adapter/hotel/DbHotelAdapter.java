package NoDam.Demo.adapter.hotel;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

// 외부 호텔 api 실패 시 fallback : db에 저장된 해당 지역의 HOTEL place 1건을 반환한다
@Slf4j
@Component
@RequiredArgsConstructor
public class DbHotelAdapter implements HotelPort {

    private final PlaceRepository placeRepository;

    @Override
    public Optional<String> recommendHotelGoogleId(Region region) {
        return placeRepository
                .findByPlaceTypeAndRegionId(PlaceType.HOTEL, region.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(Place::getGoogleId);
    }
}
