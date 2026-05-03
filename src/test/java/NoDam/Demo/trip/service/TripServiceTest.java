package NoDam.Demo.trip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TransportType;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.CreateTripRequest;
import NoDam.Demo.trip.repository.TripRepository;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private TransactionStatus transactionStatus;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(tripRepository, transactionTemplate);
    }

    @Test
    @DisplayName("여행 일정 생성 성공")
    void createTripSuccess() {
        // given
        Long userId = 1L;
        CreateTripRequest request = createRequest("uuid-1");
        stubTransactionTemplate();
        
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Trip result = tripService.createTrip(userId, request);

        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getUuid()).isEqualTo(request.getUuid());
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    @DisplayName("여행 일정 생성 중복(Idempotency) - 이미 존재하면 기존 데이터 반환")
    void createTripIdempotency() {
        // given
        Long userId = 1L;
        String uuid = "uuid-1";
        CreateTripRequest request = createRequest(uuid);
        Trip existingTrip = Trip.builder().uuid(uuid).name("Existing").build();

        stubTransactionTemplate();
        // save 시점에 중복 예외 발생 가정
        when(tripRepository.save(any(Trip.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));
        // 재조회 시 존재함
        when(tripRepository.findByUuid(uuid)).thenReturn(Optional.of(existingTrip));

        // when
        Trip result = tripService.createTrip(userId, request);

        // then
        assertThat(result).isSameAs(existingTrip);
        verify(tripRepository).findByUuid(uuid);
    }

    @Test
    @DisplayName("여행 일정 생성 실패 - 중복 예외 발생했으나 조회 결과도 없으면 예외 재발생")
    void createTripFailWhenNotFound() {
        // given
        Long userId = 1L;
        String uuid = "uuid-1";
        CreateTripRequest request = createRequest(uuid);

        stubTransactionTemplate();
        when(tripRepository.save(any(Trip.class))).thenThrow(new DataIntegrityViolationException("Conflict"));
        when(tripRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.createTrip(userId, request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("사용자 여행 리스트 조회 성공")
    void getTripListSuccess() {
        // given
        Long userId = 1L;
        List<Trip> trips = List.of(Trip.builder().name("Trip 1").build());
        when(tripRepository.findAllByUserId(userId)).thenReturn(trips);

        // when
        List<Trip> result = tripService.getTripList(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Trip 1");
    }

    private CreateTripRequest createRequest(String uuid) {
        CreateTripRequest request = new CreateTripRequest();
        request.setName("Test Trip");
        request.setUuid(uuid);
        request.setPersonCount(2);
        request.setSite("JAPAN");
        request.setScheduleType(ScheduleType.TIGHT);
        request.setTransportType(TransportType.PUBLIC);
        request.setTripThemeType(TripThemeType.FOOD);
        request.setStartDate("2026-04-27");
        request.setEndDate("2026-04-30");
        request.setPrice(100000L);
        return request;
    }

    @SuppressWarnings("unchecked")
    private void stubTransactionTemplate() {
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(transactionStatus);
        });
    }
}
