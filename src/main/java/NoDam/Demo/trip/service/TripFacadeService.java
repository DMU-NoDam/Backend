package NoDam.Demo.trip.service;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripUpdateDto;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto.FlightInfo;
import NoDam.Demo.trip.repository.UserFixedTripRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import NoDam.Demo.trip.dto.response.TripInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TripFacadeService {

    private final TripCreateService tripCreateService;
    private final TripRequestService tripRequestService;
    private final TripFixedService tripFixedService;
    private final TripSelectService tripSelectService;
    private final TripDeleteService tripDeleteService;
    private final TripMemberService tripMemberService;
    private final TransactionTemplate transactionTemplate; // deleteTrip 원자성 처리용
    private final DatePlanDBPort datePlanDBPort;
    private final TripInvitationService tripInvitationService;
    private final UserFixedTripRepository userFixedTripRepository;

    // trip domain 생성 + 요청 스냅샷(TripRequest) 저장 까지만 (ai생성은 다른 api 분리, transaction 때문!)
    // transactional (사용 금지!)
    public Trip createTrip(Long userId, TripCreateFacadeRequestDto request) {
        Trip trip = tripCreateService.createTrip(userId, request.getTrip());
        tripRequestService.create(trip.getId(), request);
        return trip;
    }

    public List<TripInfoDto> getTripList(Long userId) {
        List<Trip> trips = tripSelectService.getTripList(userId);

        return trips
                .stream()
                .map(TripInfoDto::from)
                .toList();
    }

    public TripInfoDto getTrip(Long userId, Long tripId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        return TripInfoDto.from(trip);
    }

    public Optional<TripInfoDto> getTodayTrip(Long userId) {
        Optional<Trip> tripOpt = tripFixedService.getTodayTrip(userId);

        if(tripOpt.isEmpty())
            return Optional.empty();

        return Optional.of(TripInfoDto.from(tripOpt.get()));
    }

    public Trip updateTripFixed(Long userId, Long tripId, boolean isFixed) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripFixedService.updateTripFixed(userId, trip, isFixed);
    }

    public Trip updateTripTheme(Long userId, Long tripId, TripThemeType themeType) {
        Trip trip = tripSelectService.findById(tripId, userId);
        trip = tripFixedService.updateTripTheme(trip, themeType);

        // delete other themes
        List<DatePlan> otherDatePlans = datePlanDBPort.datePlans(tripId)
                .stream()
                .filter(dp->!dp.getTripThemeType().equals(themeType))
                .toList();
         datePlanDBPort.deleteDatePlans(otherDatePlans);

        return trip;
    }

    public Trip updateTripInfo(Long userId, Long tripId, TripUpdateDto request) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripFixedService.updateTripInfo(trip, request.getName(), request.getPersonCount());
    }

    // 여행 삭제 : OWNER만 가능. 연관된 일정(DatePlan/PlacePlan/TransportPlan), TripRequest, 멤버/초대 데이터까지 함께 정리한다.
    // Place(장소 마스터 데이터)는 여러 여행이 공유하므로 삭제 대상에서 제외한다.
    // todo : delete port 따로 두기 or delete service. delete trip으로 옮기기
    public void deleteTrip(Long userId, Long tripId) {
        Trip trip = tripSelectService.findById(tripId, userId); // 멤버가 아니면 NOT_AUTHOR, 없으면 NOT_FOUND
        tripMemberService.requireOwner(tripId, userId); // 멤버지만 OWNER가 아니면 NOT_AUTHOR

        transactionTemplate.execute(status -> {
            datePlanDBPort.deleteDatePlans(datePlanDBPort.datePlans(tripId));

            tripRequestService.deleteByTripId(tripId);
            tripMemberService.deleteAllByTripId(tripId);
            tripInvitationService.deleteAllByTripId(tripId);
            userFixedTripRepository.deleteAllByTrip(trip);

            tripDeleteService.deleteTrip(trip);
            return null;
        });
    }

}
