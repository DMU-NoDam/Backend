package NoDam.Demo.plan.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.behaviour.domain.*;
import NoDam.Demo.plan.behaviour.port.BehaviourDBPort;
import NoDam.Demo.plan.behaviour.service.BehaviourService;
import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.response.DatePlanInfo;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.response.PlanStatusResponse;
import NoDam.Demo.plan.dto.response.TransportPlanInfo;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import NoDam.Demo.plan.repository.DatePlanDtoPort;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanFacadeService {

    private final TripSelectService tripSelectService;
    private final PlaceSelectService placeQueryService;
    private final BehaviourService behaviourService;

    private final DatePlanDBPort datePlanDBPort;
    private final DatePlanDtoPort datePlanDtoPort;
    private final BehaviourDBPort behaviourDBPort;
    private final TransportPlanRepository transportPlanRepository;

    public DatePlanInfo addPlacePlan(
            @NonNull Long datePlanId,
            @NonNull Long placeId,
            Long previousPlacePlanId, // can null
            Long nextPlacePlanId, // can null
            @NonNull Long clientDatePlanVersion,
            Long userId
    ) {
        // user, user<>date plan 권한 확인
        // todo : trip validate service.validateByDatePlan(Long datePlanId, Long userId);
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND)); // facade port 직접 호출? -> service로 넣기
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);

        // place 확인
        placeQueryService.findById(placeId);

        behaviourService.trySave(datePlan.getId(), clientDatePlanVersion, new AddPlaceBehaviour(previousPlacePlanId, nextPlacePlanId, placeId));

        return datePlanDtoPort.selectDatePlanInfo(datePlanDBPort.latestDatePlan(datePlanId).get());
    }

    public DatePlanInfo changePlacePlan(
            @NonNull Long datePlanId,
            @NonNull Long placePlanId,
            @NonNull Long placeId,
            @NonNull Long clientDatePlanVersion,
            Long userId
    ) {
        // user, user<>date plan 권한 확인
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND));
        tripSelectService.findById(datePlan.getTripId(), userId);

        // place 확인
        placeQueryService.findById(placeId);

        behaviourService.trySave(datePlan.getId(), clientDatePlanVersion, new ChangePlaceBehaviour(placePlanId, placeId));

        return datePlanDtoPort.selectDatePlanInfo(datePlanDBPort.latestDatePlan(datePlanId).get());
    }

    public DatePlanInfo movePlacePlan(
            @NonNull Long datePlanId,
            @NonNull Long placePlanId,
            Long previousPlacePlanId, // can null
            Long nextPlacePlanId, // can null
            @NonNull Long clientDatePlanVersion,
            Long userId
    ) {
        // user, user<>date plan 권한 확인
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND));
        tripSelectService.findById(datePlan.getTripId(), userId);

        behaviourService.trySave(datePlan.getId(), clientDatePlanVersion, new MovePlaceBehaviour(placePlanId, previousPlacePlanId, nextPlacePlanId));

        return datePlanDtoPort.selectDatePlanInfo(datePlanDBPort.latestDatePlan(datePlanId).get());
    }

    public DatePlanInfo deletePlacePlan(
            @NonNull Long datePlanId,
            @NonNull Long placePlanId,
            @NonNull Long clientDatePlanVersion,
            Long userId
    ) {
        // user, user<>date plan 권한 확인
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND));
        tripSelectService.findById(datePlan.getTripId(), userId);

        behaviourService.trySave(datePlan.getId(), clientDatePlanVersion, new RemovePlaceBehaviour(placePlanId));

        return datePlanDtoPort.selectDatePlanInfo(datePlanDBPort.latestDatePlan(datePlanId).get());
    }

    public DatePlanInfo fixPlacePlan(
            @NonNull Long datePlanId,
            @NonNull Long placePlanId,
            @NonNull Boolean isFixed,
            @NonNull Long clientDatePlanVersion,
            Long userId
    ) {
        // user, user<>date plan 권한 확인
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND));
        tripSelectService.findById(datePlan.getTripId(), userId);

        behaviourService.trySave(datePlan.getId(), clientDatePlanVersion, new FixPlaceBehaviour(placePlanId, isFixed));

        return datePlanDtoPort.selectDatePlanInfo(datePlanDBPort.latestDatePlan(datePlanId).get());
    }

    // 확정 테마의 DatePlanInfo 목록. 테마 미확정이면 빈 목록
    public List<DatePlanInfo> getPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        // todo : trip theme summary api로 옮기면 주석 해제
//        if (trip.getTripThemeType() == null)
//            return new ArrayList<>();

        return datePlanDBPort.datePlans(tripId)
                .stream()
                .map(datePlanDtoPort::selectDatePlanInfo)
                .toList();
    }

    public PlanStatusResponse getPlanStatus(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        List<DatePlan> datePlans = datePlanDBPort.datePlans(tripId);

        // DatePlan이 하나도 없으면(Trip 생성 직후) status는 null
        PlanStatus planStatus = PlanStatus.lowest(datePlans.stream().map(DatePlan::getPlanStatus).toList());

        return new PlanStatusResponse(planStatus, trip.getIsPlanning());
    }

    // polling용. 최신 version만 읽는다. 기록이 없으면 null
    public Long getLatestVersion(@NonNull Long datePlanId) {
        return behaviourDBPort.selectLatestVersion(datePlanId);
    }

    public TransportPlanInfo getTransportPlanDetail(Long transportPlanId) {
        TransportPlan transportPlan = transportPlanRepository.findById(transportPlanId)
                .orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND));
        return TransportPlanInfo.of(transportPlan);
    }

}
