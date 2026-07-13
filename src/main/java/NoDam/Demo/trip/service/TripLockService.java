package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.repository.TripStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TripLockService {

    private final TripStatusRepository tripStatusRepository;
    private final TransactionTemplate transactionTemplate;

    // trip의 isPlanning boolean으로 동시 실행을 막고 작업을 수행한다.
    // 잠금 획득(false->true)은 try 밖에서 처리하여, 획득 실패(이미 진행 중) 시
    // 다른 작업의 잠금을 해제하지 않는다. 내 작업 중 실패한 경우에만 catch에서 해제한다.
    public <T> T runWithLock(Trip trip, Supplier<T> task) {
        // 잠금 획득: false -> true. 실패(이미 진행 중)하면 여기서 종료 → 남의 잠금은 건드리지 않는다
        int affected = transactionTemplate.execute(s ->
                tripStatusRepository.tryUpdateTripStatus(trip.getId(), false, true));
        if (affected != 1)
            throw new CustomException(ErrorCode.ALREADY_PROCESSING);
        trip.updatePlanning(true);

        try {
            T result = task.get();
            transactionTemplate.execute(s -> {
                tripStatusRepository.tryUpdateTripStatusForce(trip.getId(), false);
                return null;
            });
            trip.updatePlanning(false);
            return result;
        } catch (Exception e) {
            // 내 작업 중 실패한 경우에만 해제한다
            transactionTemplate.execute(s -> {
                tripStatusRepository.tryUpdateTripStatusForce(trip.getId(), false);
                return null;
            });
            trip.updatePlanning(false);
            throw e;
        }
    }
}
