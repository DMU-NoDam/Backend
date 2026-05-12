package NoDam.Demo.trip.service;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.service.AutoCreatePlanService;
import NoDam.Demo.plan.service.PlanCreateService;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripCreateDto;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripFacadeService {

    private final TripCreateService tripCreateService;
    private final TripFixedService tripFixedService;
    private final TripSelectService tripSelectService;

    // trip domain 생성 까지만 (ai생성은 다른 api 분리, transaction 때문!)
    // transactional (사용 금지!)
    public Trip createTrip(Long userId, TripCreateDto request) {
        return tripCreateService.createTrip(userId, request);
    }

    public List<Trip> getTripList(Long userId) {
        return tripSelectService.getTripList(userId);
    }

    public Trip getTrip(Long userId, Long tripId) {
        return tripSelectService.findById(tripId, userId);
    }

    public Optional<Trip> getTodayTrip(Long userId) {
        return tripFixedService.getTodayTrip(userId);
    }

    public Trip updateTripFixed(Long userId, Long tripId, boolean isFixed) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripFixedService.updateTripFixed(userId, trip, isFixed);
    }

    public Trip updateTripTheme(Long userId, Long tripId, TripThemeType themeType) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripFixedService.updateTripTheme(trip, themeType);
    }

}
