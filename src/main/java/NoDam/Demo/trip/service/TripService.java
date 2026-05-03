package NoDam.Demo.trip.service;

import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.CreateTripRequest;
import NoDam.Demo.trip.repository.TripRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * 여행 일정 생성 (DB Unique 제약 조건 기반 멱등성 처리)
     */
    public Trip createTrip(Long userId, CreateTripRequest request) {
        Trip trip = Trip.builder()
                .name(request.getName())
                .userId(userId)
                .uuid(request.getUuid())
                .siteId(1L)
                .personCount(request.getPersonCount())
                .scheduleType(request.getScheduleType())
                .personType(request.getPersonType())
                .transportType(request.getTransportType())
                .startDate(DateUtil.toLocalDate(request.getStartDate()))
                .endDate(DateUtil.toLocalDate(request.getEndDate()))
                .price(request.getPrice())
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

    /**
     * 사용자의 여행 리스트 조회
     */
    public List<Trip> getTripList(Long userId) {
        return tripRepository.findAllByUserId(userId);
    }
}
