package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.UserFixedTrip;
import NoDam.Demo.trip.repository.TripRepository;
import NoDam.Demo.trip.repository.UserFixedTripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/*
    TripFixedService 단위 test (repository, TransactionTemplate mock)

    TransactionTemplate은 callback을 그대로 실행하도록 stub 한다
    여행 기간 : 2026-12-01 ~ 2026-12-03 (3일)
 */
@ExtendWith(MockitoExtension.class)
class TripFixedServiceTest {

    private static final Long USER_ID = 10L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 12, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 12, 3);

    @Mock
    private UserFixedTripRepository userFixedTripRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TripFixedService tripFixedService;

    @Captor
    private ArgumentCaptor<List<UserFixedTrip>> fixedTripCaptor;

    private Trip createTrip(boolean isFixed) {
        return Trip.builder()
                .name("도쿄 여행")
                .userId(USER_ID)
                .uuid("trip-uuid")
                .personCount(2)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .isFixed(isFixed)
                .build();
    }

    private void runTransactionCallback() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    private void returnSavedTrip() {
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("오늘 여행 조회")
    class GetTodayTrip {

        @Test
        @DisplayName("오늘 고정된 여행이 있으면 첫 번째 여행을 반환한다")
        void getTodayTrip() {
            Trip trip = createTrip(true);
            when(userFixedTripRepository.findAllByUserIdAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(new UserFixedTrip(USER_ID, LocalDate.now(), trip)));

            Optional<Trip> result = tripFixedService.getTodayTrip(USER_ID);

            assertTrue(result.isPresent());
            assertSame(trip, result.get());
        }

        @Test
        @DisplayName("오늘 고정된 여행이 없으면 빈 Optional을 반환한다")
        void getTodayTripEmpty() {
            when(userFixedTripRepository.findAllByUserIdAndDateRange(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of());

            assertTrue(tripFixedService.getTodayTrip(USER_ID).isEmpty());
        }
    }

    @Nested
    @DisplayName("여행 고정 변경")
    class UpdateTripFixed {

        @Test
        @DisplayName("현재 상태와 같으면 아무것도 하지 않고 그대로 반환한다")
        void updateTripFixedSameState() {
            Trip trip = createTrip(true);

            Trip result = tripFixedService.updateTripFixed(USER_ID, trip, true);

            assertSame(trip, result);
            verifyNoInteractions(transactionTemplate, userFixedTripRepository, tripRepository);
        }

        @Test
        @DisplayName("고정 : 기간 내 모든 날짜를 저장하고 fixed를 true로 바꾼다")
        void updateTripFixedTrue() {
            Trip trip = createTrip(false);
            runTransactionCallback();
            returnSavedTrip();
            when(userFixedTripRepository.findAllByUserIdAndDateRangeForUpdate(USER_ID, START_DATE, END_DATE))
                    .thenReturn(List.of());

            Trip result = tripFixedService.updateTripFixed(USER_ID, trip, true);

            verify(userFixedTripRepository).saveAll(fixedTripCaptor.capture());
            List<UserFixedTrip> saved = fixedTripCaptor.getValue();
            assertEquals(List.of(START_DATE, START_DATE.plusDays(1), END_DATE),
                    saved.stream().map(UserFixedTrip::getDate).toList());
            assertTrue(saved.stream().allMatch(fixed -> USER_ID.equals(fixed.getUserId()) && fixed.getTrip() == trip));
            assertTrue(result.isFixed());
        }

        @Test
        @DisplayName("고정 : 기간 내 이미 고정된 날짜가 있으면 CONFLICT, 저장하지 않는다")
        void updateTripFixedTrueConflict() {
            Trip trip = createTrip(false);
            Trip otherTrip = createTrip(true);
            runTransactionCallback();
            when(userFixedTripRepository.findAllByUserIdAndDateRangeForUpdate(USER_ID, START_DATE, END_DATE))
                    .thenReturn(List.of(new UserFixedTrip(USER_ID, START_DATE, otherTrip)));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripFixedService.updateTripFixed(USER_ID, trip, true));

            assertEquals(ErrorCode.CONFLICT, exception.errorCode);
            assertFalse(trip.isFixed());
            verify(userFixedTripRepository, never()).saveAll(anyList());
            verify(tripRepository, never()).save(any(Trip.class));
        }

        @Test
        @DisplayName("고정 해제 : 고정 기록을 삭제하고 fixed를 false로 바꾼다")
        void updateTripFixedFalse() {
            Trip trip = createTrip(true);
            runTransactionCallback();
            returnSavedTrip();

            Trip result = tripFixedService.updateTripFixed(USER_ID, trip, false);

            verify(userFixedTripRepository).deleteByUserIdAndTrip(USER_ID, trip);
            assertFalse(result.isFixed());
        }
    }

    @Nested
    @DisplayName("여행 정보 변경")
    class UpdateTripInfo {

        @Test
        @DisplayName("updateTripTheme : 테마가 없으면 설정한다")
        void updateTripTheme() {
            Trip trip = createTrip(false);
            returnSavedTrip();

            Trip result = tripFixedService.updateTripTheme(trip, TripThemeType.FOOD);

            assertEquals(TripThemeType.FOOD, result.getTripThemeType());
        }

        @Test
        @DisplayName("updateTripTheme : 이미 테마가 있으면 덮어쓰지 않는다")
        void updateTripThemeAlreadySet() {
            Trip trip = createTrip(false);
            trip.updateTheme(TripThemeType.HEALING);
            returnSavedTrip();

            Trip result = tripFixedService.updateTripTheme(trip, TripThemeType.FOOD);

            assertEquals(TripThemeType.HEALING, result.getTripThemeType());
        }

        @Test
        @DisplayName("updateTripInfo : 이름과 인원을 변경한다")
        void updateTripInfo() {
            Trip trip = createTrip(false);
            returnSavedTrip();

            Trip result = tripFixedService.updateTripInfo(trip, "오사카 여행", 4);

            assertEquals("오사카 여행", result.getName());
            assertEquals(4, result.getPersonCount());
        }

        @Test
        @DisplayName("updateTripInfo : 빈 이름과 null 인원은 무시한다")
        void updateTripInfoIgnoreBlank() {
            Trip trip = createTrip(false);
            returnSavedTrip();

            Trip result = tripFixedService.updateTripInfo(trip, " ", null);

            assertEquals("도쿄 여행", result.getName());
            assertEquals(2, result.getPersonCount());
        }
    }
}
