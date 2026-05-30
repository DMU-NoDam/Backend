package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.UserFixedTrip;
import NoDam.Demo.trip.repository.TripRepository;
import NoDam.Demo.trip.repository.UserFixedTripRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripFixedService {

    private final UserFixedTripRepository userFixedTripRepository;
    private final TripRepository tripRepository;
    private final TransactionTemplate transactionTemplate;

    public Optional<Trip> getTodayTrip(Long userId) {
        LocalDate now = LocalDate.now();
        List<UserFixedTrip> userFixedTripList = userFixedTripRepository.findAllByUserIdAndDateRange(userId, now, now);

        if(userFixedTripList == null || userFixedTripList.isEmpty())
            return Optional.empty();
        else
            return Optional.of(userFixedTripList.get(0).getTrip());
    }

    public Trip updateTripFixed(Long userId, Trip trip, boolean isFixed) {
        if (trip.isFixed() == isFixed) {
            return trip;
        }

        if (isFixed) {
            return setTripFixedTrue(userId, trip);
        } else {
            return setTripFixedFalse(userId, trip);
        }
    }

    private Trip setTripFixedTrue(Long userId, Trip trip) {
        List<LocalDate> dates = trip.getStartDate().datesUntil(trip.getEndDate().plusDays(1)).toList();

        return transactionTemplate.execute((status) -> {
            // 비관적 락을 통한 범위 내 중복 확인
            List<UserFixedTrip> existingTrips = userFixedTripRepository
                    .findAllByUserIdAndDateRangeForUpdate(userId, trip.getStartDate(), trip.getEndDate());

            if (!existingTrips.isEmpty()) {
                throw new CustomException(ErrorCode.CONFLICT);
            }

            // 기간 내 모든 날짜를 고정 상태로 저장
            userFixedTripRepository.saveAll(
                    dates.stream()
                            .map(date -> new UserFixedTrip(userId, date, trip))
                            .toList()
            );

            trip.updateFixed(true);
            return trip;
        });
    }

    private Trip setTripFixedFalse(Long userId, Trip trip) {
        return transactionTemplate.execute((status) -> {
            userFixedTripRepository.deleteByUserIdAndTrip(userId, trip);
            trip.updateFixed(false);
            return trip;
        });
    }

    public Trip updateTripTheme(Trip trip, TripThemeType themeType) {
        trip.updateTheme(themeType);
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip updateTripInfo(Trip trip, String name, Integer personCount) {
        trip.updateInfo(name, personCount);
        return tripRepository.save(trip);
    }
}
