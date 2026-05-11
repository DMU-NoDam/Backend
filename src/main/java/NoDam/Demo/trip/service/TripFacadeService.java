package NoDam.Demo.trip.service;

import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.service.AutoCreatePlanService;
import NoDam.Demo.plan.service.PlanCreateService;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.service.RegionQueryService;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripDate;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripCreateDto;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripFacadeService {

    private final TripCreateService tripCreateService;
    private final TripFixedService tripFixedService;
    private final TripSelectService tripSelectService;

    private final PlanCreateService planCreateService;
    private final AutoCreatePlanService autoCreatePlanService;

    private final RegionQueryService regionQueryService;

    public Trip createTrip(Long userId, TripCreateFacadeRequestDto request) {
        TripCreateDto dto = request.getTrip();
        List<Region> regions = regionQueryService.findRegionsByCode(request.getRegion());
        List<Place> userSelectedPlaces = List.of(); // todo : find place // placeSelectService.(request.getSelectedPlace());
        // List<Place> hotelPlaces = placeSelectService.(request.getHotel());
        // List<Flight>

        // create trip
        Trip trip = tripCreateService.createTrip(userId, dto);

        // 필수 plan 생성 (공항, 숙소)
            // todo : 공항 plan 생성
            // todo : 숙소 plan 생성

        // create trip dates
        List<TripDate> tripDates = tripCreateService.createTripDates(trip, regions, userSelectedPlaces);

        // auto generate plans
        autoCreatePlanService.autoGenerateAllPlan(trip, tripDates);

        return trip;
    }

    public List<Trip> getTripList(Long userId) {
        return tripSelectService.getTripList(userId);
    }

    public Optional<Trip> getTodayTrip(Long userId) {
        return tripFixedService.getTodayTrip(userId);
    }

    public Trip updateTripFixed(Long userId, Long tripId, boolean isFixed) {
        Trip trip = tripSelectService.findById(tripId, userId);
        return tripFixedService.updateTripFixed(userId, trip, isFixed);
    }

}
