package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.trip.domain.TripRequest;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto.FlightInfo;
import NoDam.Demo.trip.repository.TripRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripRequestService {

    private final TripRequestRepository tripRequestRepository;

    // trip 생성 직후 요청 스냅샷 저장 (trip domain에 담기지 않는 입력값 보관)
    // 동시성 : select-then-save 이지만 tripId unique 제약으로 정합성 보장 (createTrip 멱등성 재호출 케이스)
    public TripRequest create(Long tripId, TripCreateFacadeRequestDto request) {
        Optional<TripRequest> existing = tripRequestRepository.findByTripId(tripId);
        if (existing.isPresent())
            return existing.get(); // 멱등성 처리

        FlightInfo depart = request.getDepartFlight();
        FlightInfo arrive = request.getArriveFlight();

        TripRequest tripRequest = TripRequest.builder()
                .tripId(tripId)
                .regionCodes(request.getRegion())
                .selectedPlaceGoogleIds(request.getSelectedPlace())
                .hotelGoogleId(request.getHotel())
                .departAirportCode(depart != null ? depart.getAirport() : null)
                .departTime(depart != null ? DateUtil.toLocalDateTime(depart.getTime()) : null)
                .arriveAirportCode(arrive != null ? arrive.getAirport() : null)
                .arriveTime(arrive != null ? DateUtil.toLocalDateTime(arrive.getTime()) : null)
                .build();

        try {
            return tripRequestRepository.save(tripRequest);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 이미 저장된 경우
            return findByTripId(tripId);
        }
    }

    public TripRequest findByTripId(Long tripId) {
        return tripRequestRepository.findByTripId(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    // 변환(google 조회)이 필요한 google id 목록 반환 (필수 장소 + 호텔)
    public List<String> findGoogleIdsToConvert(Long tripId) {
        TripRequest tripRequest = findByTripId(tripId);
        List<String> googleIds = new ArrayList<>(tripRequest.getSelectedPlaceGoogleIds());
        if (tripRequest.getHotelGoogleId() != null)
            googleIds.add(tripRequest.getHotelGoogleId());
        return googleIds;
    }

    // google 조회 결과(map)로 place 변환 결과 반영 (공항은 code -> place id 정적 매핑, 엔티티 mutator는 service 내부에 감춤)
    // 필수 장소 / 호텔 : map에 값이 없으면 변환 실패로 간주해 throw
    public TripRequest updateConvertedPlaces(Long tripId, Map<String, Place> placeByGoogleId) {
        TripRequest tripRequest = findByTripId(tripId);

        List<Long> selectedPlaceIds = tripRequest.getSelectedPlaceGoogleIds().stream()
                .map(googleId -> {
                    Place place = placeByGoogleId.get(googleId);
                    if (place == null)
                        throw new CustomException(ErrorCode.NOT_FOUND);
                    return place.getId();
                })
                .toList();

        Long hotelPlaceId = null;
        if (tripRequest.getHotelGoogleId() != null) {
            Place hotel = placeByGoogleId.get(tripRequest.getHotelGoogleId());
            if (hotel == null)
                throw new CustomException(ErrorCode.NOT_FOUND);
            hotelPlaceId = hotel.getId();
        }

        Long departAirportPlaceId = tripRequest.getDepartAirportCode() != null
                ? tripRequest.getDepartAirportCode().getPlaceId() : null;
        Long arriveAirportPlaceId = tripRequest.getArriveAirportCode() != null
                ? tripRequest.getArriveAirportCode().getPlaceId() : null;

        tripRequest.updateConvertedPlaces(selectedPlaceIds, hotelPlaceId, departAirportPlaceId, arriveAirportPlaceId);
        return tripRequestRepository.save(tripRequest);
    }

    public void deleteByTripId(Long tripId) {
        tripRequestRepository.deleteByTripId(tripId);
    }

}
