package NoDam.Demo.plan.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.response.DatePlanInfo;
import NoDam.Demo.plan.repository.*;
import NoDam.Demo.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PlanSelectService {

    private final DatePlanRepository datePlanRepository;
    private final PlacePlanRepository placePlanRepository;
    private final TransportPlanRepository transportPlanRepository;

    private final DatePlanDBPort datePlanDBPort;
    private final DatePlanDtoPort datePlanDtoPort;

    public DatePlanInfo findLatestDatePlanInfo(Long datePlanId) {
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return datePlanDtoPort.selectDatePlanInfo(datePlan);
    }

    // 확정 테마의 DatePlanInfo 목록
    @Transactional
    public List<DatePlanInfo> findDatePlanInfos(Trip trip, TripThemeType tripThemeType) {
        List<DatePlan> datePlans = datePlanDBPort.datePlans(trip.getId());
        List<DatePlanInfo> datePlanInfos = new ArrayList<>();

        for(DatePlan each : datePlans) {
            if(!each.getTripThemeType().equals(tripThemeType))
                continue;

            datePlanInfos.add(datePlanDtoPort.selectDatePlanInfo(each));
        }

        return datePlanInfos;
    }

    public List<DatePlan> findAllDatePlan(Trip trip) {
        return datePlanRepository.findAllDatePlanWithPlans(trip.getId());
    }

    public List<DatePlan> findAllDatePlanWithTransport(Trip trip) {
        return datePlanRepository.findAllDatePlanWithPlansWithTransport(trip.getId());
    }

    public List<PlacePlan> findPlacePlansByDatePlan(DatePlan datePlan) {
        return placePlanRepository.findByDatePlanId(datePlan.getId());
    }

    public List<PlacePlan> findPlacePlansByDatePlanWithTransport(DatePlan datePlan) {
        return placePlanRepository.findByDatePlanIdWithTransport(datePlan.getId());
    }

    public PlacePlan findPlacePlanWithDatePlanAndTransport(Long placePlanId) {
        return placePlanRepository.findByIdWithDatePlanAndTransport(placePlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    public List<Place> findPlacedPlaces(Trip trip, TripThemeType theme) {
        return placePlanRepository.findPlacesByTripIdAndTheme(trip.getId(), theme);
    }

    public boolean hasTransportPlan(DatePlan datePlan) {
        return !transportPlanRepository.findByFromPlacePlan_DatePlanId(datePlan.getId()).isEmpty();
    }

    public TransportPlan findTransportPlanById(Long transportPlanId) {
        return transportPlanRepository.findByIdWithDatePlan(transportPlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    // DatePlan이 하나도 없는 trip도 빈 list를 value로 가진다 (key 누락 방지)
    public Map<Trip, List<DatePlan>> findAllByTrip(List<Trip> trips) {
        List<Long> tripIds = trips.stream().map(Trip::getId).toList();
        Map<Long, Trip> tripById = trips.stream().collect(Collectors.toMap(Trip::getId, Function.identity()));

        Map<Trip, List<DatePlan>> datePlansByTrip = new HashMap<>();
        for (Trip trip : trips)
            datePlansByTrip.put(trip, new ArrayList<>());

        for (DatePlan datePlan : datePlanRepository.findAllByTripIdIn(tripIds))
            datePlansByTrip.get(tripById.get(datePlan.getTripId())).add(datePlan);

        return datePlansByTrip;
    }

    // todo : domain 로직이 db담당인 query service에 들어왔음, ddd나 domain service로 옮길 것!
    public Map<Trip, Boolean> getTripStatus(List<Trip> trips) {
        Map<Trip, List<DatePlan>> datePlans = findAllByTrip(trips);
        Map<Trip, Boolean> tripStatus = new HashMap<>();

        for(Trip eachTrip : datePlans.keySet()) {
            List<DatePlan> datePlan = datePlans.get(eachTrip);

            boolean status = !datePlan.isEmpty(); // DatePlan이 없으면 아직 일정 생성 전이므로 false
            for (DatePlan each : datePlan) { // 한개라도 ai planned가 아닌게 있다면 false
                if (!each.getPlanStatus().isAfterOrEqual(PlanStatus.AI_PLANNED)) {
                    status = false;
                    break;
                }
            }

            tripStatus.put(eachTrip, status);
        }

        return tripStatus;
    }

    public Boolean getTripStatus(Trip trip) {
        List<DatePlan> datePlans = findAllDatePlan(trip);

        boolean status = !datePlans.isEmpty(); // DatePlan이 없으면 아직 일정 생성 전이므로 false
        for (DatePlan each : datePlans) { // 한개라도 ai planned가 아닌게 있다면 false
            if (!each.getPlanStatus().isAfterOrEqual(PlanStatus.AI_PLANNED)) {
                status = false;
                break;
            }
        }

        return status;
    }

}
