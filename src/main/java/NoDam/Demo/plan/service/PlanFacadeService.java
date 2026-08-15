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

    private final PlanSelectService planSelectService;
    private final DatePlanDBPort datePlanDBPort;
    private final BehaviourDBPort behaviourDBPort;

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

        return planSelectService.findLatestDatePlanInfo(datePlanId);
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

        return planSelectService.findLatestDatePlanInfo(datePlanId);
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

        return planSelectService.findLatestDatePlanInfo(datePlanId);
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

        return planSelectService.findLatestDatePlanInfo(datePlanId);
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

        return planSelectService.findLatestDatePlanInfo(datePlanId);
    }

    // 확정 테마의 DatePlanInfo 목록. 테마 미확정이면 빈 목록
    public List<DatePlanInfo> getPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);

        if (trip.getTripThemeType() == null)
            return new ArrayList<>();

        return planSelectService.findDatePlanInfos(trip, trip.getTripThemeType());
    }

    public PlanStatusResponse getPlanStatus(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        List<DatePlan> datePlans = planSelectService.findAllDatePlan(trip);

        // DatePlan이 하나도 없으면(Trip 생성 직후) status는 null
        PlanStatus planStatus = PlanStatus.lowest(datePlans.stream().map(DatePlan::getPlanStatus).toList());

        return new PlanStatusResponse(planStatus, trip.getIsPlanning());
    }

    // polling용. 최신 version만 읽는다. 기록이 없으면 null
    public Long getLatestVersion(@NonNull Long datePlanId) {
        return behaviourDBPort.selectLatestVersion(datePlanId);
    }

    public TransportPlanInfo getTransportPlanDetail(Long transportPlanId) {
        TransportPlan transportPlan = planSelectService.findTransportPlanById(transportPlanId);
        return TransportPlanInfo.of(transportPlan);
    }

}
