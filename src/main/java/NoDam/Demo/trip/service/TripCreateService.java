package NoDam.Demo.trip.service;

import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.TripCreateDto;
import NoDam.Demo.trip.repository.TripRepository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripCreateService {

    private final TripRepository tripRepository;
    private final TransactionTemplate transactionTemplate; // createTrip 멱등성 처리용

    /**
     * 여행 일정 생성 (DB Unique 제약 조건 기반 멱등성 처리)
     */
    public Trip createTrip(Long userId, TripCreateDto request) {
        Trip trip = Trip.builder()
                .name(request.getName())
                .userId(userId)
                .uuid(request.getUuid())
                .personCount(request.getPersonCount())
                .scheduleType(ScheduleType.LOOSE)
                .priceType(request.getPriceType())
                .startDate(DateUtil.toLocalDate(request.getStartDate()))
                .endDate(DateUtil.toLocalDate(request.getEndDate()))
                .build();

        try {
            return transactionTemplate.execute(status -> tripRepository.save(trip));
        } catch (DataIntegrityViolationException e) {
            // 동시성 확인
            Optional<Trip> tripOptional = tripRepository.findByUuid(request.getUuid());
            if (tripOptional.isPresent()) {
                return tripOptional.get();
            }
            throw e;
        }
    }

}
