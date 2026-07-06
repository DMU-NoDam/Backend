package NoDam.Demo.adapter.route;

import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.dto.PlaceInfo;
import NoDam.Demo.plan.dto.response.RouteInfo;

import java.time.LocalTime;

public interface RoutePort {

    RouteInfo computeRoutesFromCoord(Double startLat, Double startLon, PlaceInfo end, LocalTime startTime);

    RouteInfo computeRoutesFromPlace(Place start, Place end, LocalTime startTime);

}
