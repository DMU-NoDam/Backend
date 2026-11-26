package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.repository.TripStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    TripLockService 단위 test (TripStatusRepository, TransactionTemplate mock)

    TransactionTemplate은 callback을 그대로 실행하도록 stub 한다
    잠금 획득 : tryUpdateTripStatus(id, false, true) 결과가 1이면 성공, 그 외는 이미 진행 중
    trip id는 DB가 만드는 값이라 리플렉션으로 넣는다
 */
@ExtendWith(MockitoExtension.class)
class TripLockServiceTest {

    private static final Long TRIP_ID = 1L;

    @Mock
    private TripStatusRepository tripStatusRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TripLockService tripLockService;

    private Trip trip;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .name("도쿄 여행")
                .userId(10L)
                .uuid("uuid-1")
                .personCount(2)
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 3))
                .isFixed(false)
                .build();
        ReflectionTestUtils.setField(trip, "id", TRIP_ID);

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Nested
    @DisplayName("잠금 획득 성공")
    class LockAcquired {

        @Test
        @DisplayName("작업 결과를 반환하고 잠금을 해제한다")
        void returnResultAndRelease() {
            when(tripStatusRepository.tryUpdateTripStatus(TRIP_ID, false, true)).thenReturn(1);

            String result = tripLockService.runWithLock(trip, () -> "done");

            assertEquals("done", result);
            verify(tripStatusRepository).tryUpdateTripStatusForce(TRIP_ID, false);
            assertFalse(trip.getIsPlanning());
        }

        @Test
        @DisplayName("작업 실행 중에는 trip의 isPlanning이 true다")
        void planningDuringTask() {
            when(tripStatusRepository.tryUpdateTripStatus(TRIP_ID, false, true)).thenReturn(1);
            AtomicBoolean planningInTask = new AtomicBoolean(false);

            tripLockService.runWithLock(trip, () -> {
                planningInTask.set(trip.getIsPlanning());
                return null;
            });

            assertTrue(planningInTask.get());
            assertFalse(trip.getIsPlanning());
        }

        @Test
        @DisplayName("작업 중 예외가 나면 잠금을 해제하고 같은 예외를 다시 던진다")
        void releaseOnTaskFailure() {
            when(tripStatusRepository.tryUpdateTripStatus(TRIP_ID, false, true)).thenReturn(1);
            CustomException taskException = new CustomException(ErrorCode.NOT_FOUND);

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripLockService.runWithLock(trip, () -> {
                        throw taskException;
                    }));

            assertSame(taskException, exception);
            verify(tripStatusRepository).tryUpdateTripStatusForce(TRIP_ID, false);
            assertFalse(trip.getIsPlanning());
        }
    }

    @Nested
    @DisplayName("잠금 획득 실패")
    class LockNotAcquired {

        @Test
        @DisplayName("이미 진행 중이면 ALREADY_PROCESSING, 작업을 실행하지 않는다")
        void alreadyProcessing() {
            when(tripStatusRepository.tryUpdateTripStatus(TRIP_ID, false, true)).thenReturn(0);
            AtomicInteger taskCallCount = new AtomicInteger(0);

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripLockService.runWithLock(trip, () -> taskCallCount.incrementAndGet()));

            assertEquals(ErrorCode.ALREADY_PROCESSING, exception.errorCode);
            assertEquals(0, taskCallCount.get());
        }

        @Test
        @DisplayName("획득 실패 시 다른 작업의 잠금을 해제하지 않는다")
        void notReleaseOthersLock() {
            when(tripStatusRepository.tryUpdateTripStatus(TRIP_ID, false, true)).thenReturn(0);

            assertThrows(CustomException.class,
                    () -> tripLockService.runWithLock(trip, () -> "done"));

            verify(tripStatusRepository, never()).tryUpdateTripStatusForce(anyLong(), anyBoolean());
            assertFalse(trip.getIsPlanning());
        }
    }
}
