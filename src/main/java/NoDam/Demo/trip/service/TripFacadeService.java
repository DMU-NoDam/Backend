package NoDam.Demo.trip.service;

import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.TripCreateDto;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto.FlightInfo;

import java.time.LocalTime;
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
    private final PlaceSelectService placeSelectService;

    // trip domain 생성 까지만 (ai생성은 다른 api 분리, transaction 때문!)
    // transactional (사용 금지!)
    public Trip createTrip(Long userId, TripCreateDto request) {
        return tripCreateService.createTrip(userId, request);
    }

    // 공항/숙소 place 및 시간 resolve (DatePlan 생성 시 사용)
    public record FlightHotelInfo(
            Place destinationAirport,
            LocalTime firstDayAirportTime,
            LocalTime lastDayAirportTime
    ) {}

    public FlightHotelInfo resolveFlightInfo(TripCreateFacadeRequestDto request) {
        FlightInfo depart = request.getDepartFlight();
        FlightInfo arrive = request.getArriveFlight();

        Place destinationAirport = (arrive != null && arrive.getAirport() != null)
                ? placeSelectService.findById(arrive.getAirport().getPlaceId()) : null;
        LocalTime firstDayAirportTime = (depart != null && depart.getTime() != null)
                ? TimeUtil.ceilToNextHour(TimeUtil.parseTimeFromDateTime(depart.getTime())) : null;
        LocalTime lastDayAirportTime = (arrive != null && arrive.getTime() != null)
                ? TimeUtil.ceilToNextHour(TimeUtil.parseTimeFromDateTime(arrive.getTime())) : null;

        return new FlightHotelInfo(destinationAirport, firstDayAirportTime, lastDayAirportTime);
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
