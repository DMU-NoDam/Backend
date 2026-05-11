package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.region.domain.Region;
import org.springframework.transaction.annotation.Transactional;
import NoDam.Demo.region.repository.RegionRepository;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripDate;
import NoDam.Demo.trip.dto.request.TripCreateDto;
import NoDam.Demo.trip.repository.TripDateRepository;
import NoDam.Demo.trip.repository.TripRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripCreateService {

    private final TripRepository tripRepository;
    private final TripDateRepository tripDateRepository;
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
                .scheduleType(request.getScheduleType())
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

    /**
     * 날짜 수만큼 TripDate 생성, region은 [0] 만 배정ㅋ
     */
    @Transactional
    public List<TripDate> createTripDates(
            Trip trip,
            List<Region> regions, // can empty
            List<Place> selectedPlaceGoogleIds
    ) {
        List<TripDate> tripDates = tripDateRepository.findAllByTrip(trip);

        if(tripDates == null || !tripDates.isEmpty())
            return trip.getTripDates(); // 멱등성 처리

        tripDates = new ArrayList<>();
        List<LocalDate> dates = DateUtil.toDateRange(trip.getStartDate(), trip.getEndDate());

        for(int i = 0; i < dates.size(); i++) {
            // todo: selectedPlaceGoogleIds 일별 배정 로직 추가
            // todo : region 배정 일정 로직 추가
            tripDates.add(new TripDate(dates.get(i), trip, 1L , List.of()));
        }

        return tripDateRepository.saveAll(tripDates);
    }
}
