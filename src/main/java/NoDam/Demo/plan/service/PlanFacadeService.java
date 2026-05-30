package NoDam.Demo.plan.service;

import NoDam.Demo.common.type.PlanStatus;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.response.PlanStatusResponse;
import NoDam.Demo.plan.dto.response.TransportPlanInfo;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanFacadeService {

    private final PlanSelectService planSelectService;
    private final PlanCreateService planCreateService;
    private final PlanDeleteService planDeleteService;
    private final AutoCreatePlanService autoCreatePlanService;
    private final TripSelectService tripSelectService;
    private final PlaceSelectService placeQueryService;

    public Map<TripThemeType, List<PlacePlanInfo>> getPlans(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        List<DatePlan> datePlans = planSelectService.findAllDatePlanWithTransport(trip);

        List<PlacePlan> allPlacePlans = datePlans.stream()
                .flatMap(dp -> dp.getPlacePlans().stream())
                .toList();

        Map<Long, Place> placeMap = placeQueryService.findAllById(
                allPlacePlans.stream().map(PlacePlan::getPlaceId).toList()
        ).stream().collect(Collectors.toMap(Place::getId, p -> p));

        Map<TripThemeType, List<PlacePlanInfo>> result = new HashMap<>();
        for (DatePlan dp : datePlans) {
            List<PlacePlanInfo> infos = result.computeIfAbsent(dp.getTripThemeType(), k -> new ArrayList<>());
            for (PlacePlan pp : dp.getPlacePlans()) {
                infos.add(PlacePlanInfo.of(pp, placeMap.get(pp.getPlaceId())));
            }
        }
        for (List<PlacePlanInfo> infos : result.values()) {
            infos.sort(Comparator.comparing(PlacePlanInfo::getDate).thenComparing(PlacePlanInfo::getStartTime));
        }
        return result;
    }

    public PlanStatusResponse getPlanStatus(Long tripId, Long userId) {
        Trip trip = tripSelectService.findById(tripId, userId);
        List<DatePlan> datePlans = planSelectService.findAllDatePlan(trip);

        boolean allCompleted = !datePlans.isEmpty() &&
                datePlans.stream().allMatch(dp -> dp.getPlanStatus().isAfterOrEqual(PlanStatus.TRANSPORT_PLANNED));

        return new PlanStatusResponse(allCompleted, trip.getIsPlanning());
    }

    public void deletePlacePlan(Long placePlanId, Long userId) {
        PlacePlan placePlan = planSelectService.findPlacePlanWithDatePlanAndTransport(placePlanId);
        tripSelectService.findById(placePlan.getDatePlan().getTripId(), userId);

        planDeleteService.deletePlacePlanWithTransports(placePlan.getId());
    }

    public TransportPlanInfo getTransportPlanDetail(Long transportPlanId) {
        TransportPlan transportPlan = planSelectService.findTransportPlanById(transportPlanId);
        return TransportPlanInfo.of(transportPlan);
    }

    public PlacePlanInfo changePlacePlan(Long oldPlacePlanId, Long newPlaceId, Long userId) {
        PlacePlan oldPlacePlan = planSelectService.findPlacePlanWithDatePlanAndTransport(oldPlacePlanId);
        DatePlan datePlan = oldPlacePlan.getDatePlan();
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);
        Place newPlace = placeQueryService.findById(newPlaceId);

        PlacePlan newPlacePlan = changePlacePlan(datePlan, trip, oldPlacePlan, newPlace.getId());

        return PlacePlanInfo.of(newPlacePlan, newPlace);
    }

    public void switchPlacePlan(Long placePlanId1, Long placePlanId2, Long userId) {
        PlacePlan first = planSelectService.findPlacePlanWithDatePlanAndTransport(placePlanId1);
        PlacePlan second = planSelectService.findPlacePlanWithDatePlanAndTransport(placePlanId2);

        DatePlan datePlan = first.getDatePlan();
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);

        Long firstPlaceId = first.getPlaceId();
        Long secondPlaceId = second.getPlaceId();

        first = changePlacePlan(datePlan, trip, first, secondPlaceId);
        second = changePlacePlan(datePlan, trip, second, firstPlaceId);
    }

    private PlacePlan changePlacePlan(DatePlan datePlan, Trip trip, PlacePlan oldPlacePlan, Long newPlaceId) {
        planDeleteService.deletePlacePlanWithTransports(oldPlacePlan.getId());

        PlacePlan newPlacePlan = planCreateService.createPlacePlan(
                datePlan,
                newPlaceId,
                oldPlacePlan.getStartTime(),
                oldPlacePlan.getEndTime()
        );

        autoCreatePlanService.createAllTransportPlan(trip, datePlan);

        return newPlacePlan;
    }

}
