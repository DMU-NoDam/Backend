package NoDam.Demo.place.service;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AirportSelectService {

    private final PlaceRepository placeRepository;

    // TODO: region별 공항 2개 이상인 지역 처리
    public Place findAirportByRegion(Region region) {
        List<Place> airports = placeRepository.findByPlaceTypeAndRegionId(
                PlaceType.AIRPORT, region.getId(), PageRequest.of(0, 1));

        if(airports.isEmpty())
            throw new RuntimeException("air port db is not found region = " + region.getName());

        return airports.getFirst(); // todo : 2개 이상인 경우 가중치?
    }

}
