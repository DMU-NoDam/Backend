package NoDam.Demo.plan.service;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.service.TripSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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

        return datePlans.stream()
                .collect(Collectors.groupingBy(DatePlan::getTripThemeType))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap(dp -> dp.getPlacePlans().stream())
                                .sorted(Comparator.comparing(pp -> pp.getDatePlan().getDate()))
                                .map(pp -> PlacePlanInfo.of(pp, placeMap.get(pp.getPlaceId())))
                                .collect(Collectors.toList())
                ));
    }

    public void deletePlacePlan(Long placePlanId, Long userId) {
        PlacePlan placePlan = planSelectService.findPlacePlanWithDatePlanAndTransport(placePlanId);
        tripSelectService.findById(placePlan.getDatePlan().getTripId(), userId);

        planDeleteService.deletePlacePlanWithTransports(placePlan);
    }

    public PlacePlanInfo changePlacePlan(Long oldPlacePlanId, Long newPlaceId, Long userId) {
        PlacePlan oldPlacePlan = planSelectService.findPlacePlanWithDatePlanAndTransport(oldPlacePlanId);
        DatePlan datePlan = oldPlacePlan.getDatePlan();
        Trip trip = tripSelectService.findById(datePlan.getTripId(), userId);
        Place newPlace = placeQueryService.findById(newPlaceId);

        planDeleteService.deletePlacePlanWithTransports(oldPlacePlan);

        PlacePlan newPlacePlan = planCreateService.createPlacePlan(
                datePlan,
                newPlace.getId(),
                oldPlacePlan.getStartTime(),
                oldPlacePlan.getEndTime()
        );

        autoCreatePlanService.createAllTransportPlan(trip, datePlan);

        return PlacePlanInfo.of(newPlacePlan, newPlace);
    }

}
