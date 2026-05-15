package NoDam.Demo.place.service;

import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.google.GooglePlaceInfo;
import NoDam.Demo.place.dto.PlaceRequestDto;
import NoDam.Demo.region.service.RegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlaceFacadeService {

    private final PlaceSelectService placeSelectService;
    private final PlaceQueryService placeQueryService;
    private final GoogleApiService googleApiService;
    private final RegionQueryService regionQueryService;

    @Async
    public CompletableFuture<List<Place>> findAllByGoogleId(List<String> googleIds) {
        if(googleIds == null || googleIds.isEmpty())
            return CompletableFuture.completedFuture(List.of());

        if(googleIds.contains(null))
            throw new RuntimeException("PlaceFacadeService.findAllByGoogle :: parameter googleIds contain null");

        // DB에서 조회 후 요청 순서대로 정렬 (누락된 장소는 null로 표시됨)
        List<Place> selectedPlaces = ListUtil.sortByRequestOrder(googleIds, placeSelectService.findAllByGoogleId(googleIds), Place::getGoogleId);

        if(selectedPlaces.contains(null)) {
            // DB에 저장되지 않은 place가 존재함 -> missing id 추출
            List<String> notSavedGooglePlaceIds = IntStream.range(0, googleIds.size())
                    .filter(i -> selectedPlaces.get(i) == null)
                    .mapToObj(googleIds::get)
                    .toList();

            List<Place> savedPlaces = saveNewPlaces(notSavedGooglePlaceIds);
            
            // null 제거 후 새로 저장된 장소 추가
            selectedPlaces.removeIf(java.util.Objects::isNull);
            selectedPlaces.addAll(savedPlaces);
        }

        return CompletableFuture.completedFuture(ListUtil.sortByRequestOrder(googleIds, selectedPlaces, Place::getGoogleId));
    }

    private List<Place> saveNewPlaces(List<String> notSavedGoogleIds) {
        List<GooglePlaceInfo> newGooglePlaces = notSavedGoogleIds
                .stream()
                .map(googleId->googleApiService.searchByGoogleId(googleId))
                .toList();
        List<PlaceRequestDto> placeRequestDtos = new ArrayList<>();

        List<PlaceRequestDto> requestDtos =  newGooglePlaces
                .stream()
                .map(googleDto ->
                        googleDto.toPlaceDto(regionQueryService.findByCoordinate(googleDto.getLat(), googleDto.getLon()))
                )
                .toList();

        return placeQueryService.saveAll(requestDtos);
    }

}
