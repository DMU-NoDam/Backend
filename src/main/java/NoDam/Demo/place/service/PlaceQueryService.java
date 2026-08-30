package NoDam.Demo.place.service;

import NoDam.Demo.common.type.PlaceStatus;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {

    private final PlaceRepository placeRepository;

    public List<Place> saveAll(List<PlaceRequestDto> dtos) {
        return placeRepository.saveAll(dtos
                .stream()
                .map(dto-> Place
                        .builder()
                        .status(PlaceStatus.CREATED) // 앱 lazy 생성 → 크롤 전 상태
                        .regionId(dto.getRegion().getId())
                        .placeType(dto.getPlaceType())
                        .googleId(dto.getGoogleId())
                        .name(dto.getName())
                        .address(dto.getAddress())
                        .lon(dto.getLon())
                        .lat(dto.getLat())
                        .recommendWeatherType(dto.getWeatherType())
                        .recommendTripThemeType(dto.getTripThemeType())
                        .recommendSeasonType(dto.getSeasonType())
                        .priceType(dto.getPriceType())
                        .build()
                )
                .toList()
        );
    }

}
