package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.flight.service.AirportCode;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.trip.domain.TripRequest;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto.FlightInfo;
import NoDam.Demo.trip.repository.TripRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    TripRequestService 단위 test (TripRequestRepository mock)

    save는 넘겨받은 entity를 그대로 반환하도록 stub 한다
    Place는 id를 DB가 만들기 때문에 mock으로 getId만 stub 한다
    공항 place id는 AirportCode enum의 정적 매핑 값을 사용한다 (ICN -> 1, NRT -> 10)
 */
@ExtendWith(MockitoExtension.class)
class TripRequestServiceTest {

    private static final Long TRIP_ID = 1L;

    @Mock
    private TripRequestRepository tripRequestRepository;

    @InjectMocks
    private TripRequestService tripRequestService;

    private FlightInfo createFlight(AirportCode airport, String time) {
        FlightInfo flightInfo = new FlightInfo();
        flightInfo.setAirport(airport);
        flightInfo.setTime(time);
        return flightInfo;
    }

    private TripRequest createTripRequest(List<String> selectedPlaceGoogleIds, String hotelGoogleId,
                                          AirportCode depart, AirportCode arrive) {
        return TripRequest.builder()
                .tripId(TRIP_ID)
                .regionCodes(List.of("jp-tokyo"))
                .selectedPlaceGoogleIds(selectedPlaceGoogleIds)
                .hotelGoogleId(hotelGoogleId)
                .departAirportCode(depart)
                .arriveAirportCode(arrive)
                .build();
    }

    private Place createPlace(Long id) {
        Place place = mock(Place.class);
        when(place.getId()).thenReturn(id);
        return place;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("요청 값을 스냅샷으로 저장한다 (항공편 시간 문자열은 LocalDateTime으로 변환)")
        void saveSnapshot() {
            TripCreateFacadeRequestDto request = new TripCreateFacadeRequestDto();
            request.setRegion(List.of("jp-tokyo"));
            request.setSelectedPlace(List.of("g-1", "g-2"));
            request.setHotel("g-hotel");
            request.setDepartFlight(createFlight(AirportCode.ICN, "2026-12-01 09:30"));
            request.setArriveFlight(createFlight(AirportCode.NRT, "2026-12-01 12:00"));

            when(tripRequestRepository.findByTripId(TRIP_ID)).thenReturn(Optional.empty());
            when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            tripRequestService.create(TRIP_ID, request);

            ArgumentCaptor<TripRequest> captor = ArgumentCaptor.forClass(TripRequest.class);
            verify(tripRequestRepository).save(captor.capture());
            TripRequest saved = captor.getValue();

            assertEquals(TRIP_ID, saved.getTripId());
            assertEquals(List.of("jp-tokyo"), saved.getRegionCodes());
            assertEquals(List.of("g-1", "g-2"), saved.getSelectedPlaceGoogleIds());
            assertEquals("g-hotel", saved.getHotelGoogleId());
            assertEquals(AirportCode.ICN, saved.getDepartAirportCode());
            assertEquals(LocalDateTime.of(2026, 12, 1, 9, 30), saved.getDepartTime());
            assertEquals(AirportCode.NRT, saved.getArriveAirportCode());
            assertEquals(LocalDateTime.of(2026, 12, 1, 12, 0), saved.getArriveTime());
        }

        @Test
        @DisplayName("항공편 정보가 없으면 공항 / 시간은 null로 저장한다")
        void saveWithoutFlight() {
            TripCreateFacadeRequestDto request = new TripCreateFacadeRequestDto();
            request.setRegion(List.of("jp-tokyo"));

            when(tripRequestRepository.findByTripId(TRIP_ID)).thenReturn(Optional.empty());
            when(tripRequestRepository.save(any(TripRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TripRequest saved = tripRequestService.create(TRIP_ID, request);

            assertNull(saved.getDepartAirportCode());
            assertNull(saved.getDepartTime());
            assertNull(saved.getArriveAirportCode());
            assertNull(saved.getArriveTime());
            assertEquals(List.of(), saved.getSelectedPlaceGoogleIds());
        }

        @Test
        @DisplayName("이미 저장된 요청이 있으면 저장하지 않고 기존 값을 반환한다 (멱등성)")
        void idempotent() {
            TripRequest existing = createTripRequest(List.of(), null, null, null);
            when(tripRequestRepository.findByTripId(TRIP_ID)).thenReturn(Optional.of(existing));

            TripRequest result = tripRequestService.create(TRIP_ID, new TripCreateFacadeRequestDto());

            assertSame(existing, result);
            verify(tripRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("동시 요청으로 unique 제약에 걸리면 먼저 저장된 값을 다시 조회해 반환한다")
        void concurrentSave() {
            TripCreateFacadeRequestDto request = new TripCreateFacadeRequestDto();
            request.setRegion(List.of("jp-tokyo"));
            TripRequest savedByOther = createTripRequest(List.of(), null, null, null);

            when(tripRequestRepository.findByTripId(TRIP_ID))
                    .thenReturn(Optional.empty(), Optional.of(savedByOther));
            when(tripRequestRepository.save(any(TripRequest.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate trip_id"));

            assertSame(savedByOther, tripRequestService.create(TRIP_ID, request));
        }
    }

    @Nested
    @DisplayName("조회")
    class Find {

        @Test
        @DisplayName("findByTripId : 없으면 NOT_FOUND")
        void findByTripIdNotFound() {
            when(tripRequestRepository.findByTripId(TRIP_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripRequestService.findByTripId(TRIP_ID));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }

        @Test
        @DisplayName("findGoogleIdsToConvert : 필수 장소 뒤에 호텔 google id를 붙인다")
        void googleIdsWithHotel() {
            when(tripRequestRepository.findByTripId(TRIP_ID))
                    .thenReturn(Optional.of(createTripRequest(List.of("g-1", "g-2"), "g-hotel", null, null)));

            assertEquals(List.of("g-1", "g-2", "g-hotel"), tripRequestService.findGoogleIdsToConvert(TRIP_ID));
        }

        @Test
        @DisplayName("findGoogleIdsToConvert : 호텔이 없으면 필수 장소만 반환한다")
        void googleIdsWithoutHotel() {
            when(tripRequestRepository.findByTripId(TRIP_ID))
                    .thenReturn(Optional.of(createTripRequest(List.of("g-1"), null, null, null)));

            assertEquals(List.of("g-1"), tripRequestService.findGoogleIdsToConvert(TRIP_ID));
        }
    }

    @Nested
    @DisplayName("updateConvertedPlaces")
    class UpdateConvertedPlaces {

        @Test
        @DisplayName("google id -> place id, 공항 code -> place id 변환 결과를 반영한다")
        void update() {
            TripRequest tripRequest = createTripRequest(List.of("g-2", "g-1"), "g-hotel", AirportCode.ICN, AirportCode.NRT);
            when(tripRequestRepository.findByTripId(TRIP_ID)).thenReturn(Optional.of(tripRequest));
            when(tripRequestRepository.save(tripRequest)).thenReturn(tripRequest);

            Map<String, Place> placeByGoogleId = Map.of(
                    "g-1", createPlace(101L),
                    "g-2", createPlace(102L),
                    "g-hotel", createPlace(200L)
            );

            TripRequest result = tripRequestService.updateConvertedPlaces(TRIP_ID, placeByGoogleId);

            assertEquals(List.of(102L, 101L), result.getSelectedPlaceIds());
            assertEquals(200L, result.getHotelPlaceId());
            assertEquals(AirportCode.ICN.getPlaceId(), result.getDepartAirportPlaceId());
            assertEquals(AirportCode.NRT.getPlaceId(), result.getArriveAirportPlaceId());
        }

        @Test
        @DisplayName("호텔 / 공항이 없으면 해당 place id는 null이다")
        void updateWithoutHotelAndAirport() {
            TripRequest tripRequest = createTripRequest(List.of("g-1"), null, null, null);
            when(tripRequestRepository.findByTripId(TRIP_ID)).thenReturn(Optional.of(tripRequest));
            when(tripRequestRepository.save(tripRequest)).thenReturn(tripRequest);

            TripRequest result = tripRequestService.updateConvertedPlaces(TRIP_ID, Map.of("g-1", createPlace(101L)));

            assertEquals(List.of(101L), result.getSelectedPlaceIds());
            assertNull(result.getHotelPlaceId());
            assertNull(result.getDepartAirportPlaceId());
            assertNull(result.getArriveAirportPlaceId());
        }

        @Test
        @DisplayName("필수 장소 변환 결과가 없으면 NOT_FOUND, 저장하지 않는다")
        void selectedPlaceNotConverted() {
            when(tripRequestRepository.findByTripId(TRIP_ID))
                    .thenReturn(Optional.of(createTripRequest(List.of("g-1", "g-missing"), null, null, null)));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripRequestService.updateConvertedPlaces(TRIP_ID, Map.of("g-1", createPlace(101L))));

            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
            verify(tripRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("호텔 변환 결과가 없으면 NOT_FOUND, 저장하지 않는다")
        void hotelNotConverted() {
            when(tripRequestRepository.findByTripId(TRIP_ID))
                    .thenReturn(Optional.of(createTripRequest(List.of("g-1"), "g-hotel", null, null)));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripRequestService.updateConvertedPlaces(TRIP_ID, Map.of("g-1", createPlace(101L))));

            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
            verify(tripRequestRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("deleteByTripId : repository에 삭제를 위임한다")
    void deleteByTripId() {
        tripRequestService.deleteByTripId(TRIP_ID);

        verify(tripRequestRepository).deleteByTripId(TRIP_ID);
    }
}
