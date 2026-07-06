package NoDam.Demo.adapter.route.dto;

import NoDam.Demo.place.domain.Coordinate;
import NoDam.Demo.plan.dto.response.RouteInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class GoogleRouteResponseDto {

    private List<Route> routes;

    @Getter
    @Setter
    @ToString
    public static class Route {
        private Integer distanceMeters;
        private String staticDuration;
        private Polyline polyline;
        private List<Leg> legs;
    }

    @Getter
    @Setter
    @ToString
    public static class Leg {
        private Integer distanceMeters;
        private String staticDuration;
        private Polyline polyline;
        private LocationPoint startLocation;
        private LocationPoint endLocation;
        private List<Step> steps;
    }

    @Getter
    @Setter
    @ToString
    public static class Step {
        private Integer distanceMeters;
        private String staticDuration;
        private Polyline polyline;
        private LocationPoint startLocation;
        private LocationPoint endLocation;
        private String travelMode;
        private TransitDetails transitDetails;
    }

    @Getter
    @Setter
    @ToString
    public static class TransitDetails {
        private StopDetails stopDetails;
        private TransitLine transitLine;
        private Integer stopCount;
    }

    @Getter
    @Setter
    @ToString
    public static class StopDetails {
        private StopInfo arrivalStop;
        private String arrivalTime;
        private StopInfo departureStop;
        private String departureTime;
    }

    @Getter
    @Setter
    @ToString
    public static class StopInfo {
        private String name;
        private LocationPoint location;
    }

    @Getter
    @Setter
    @ToString
    public static class TransitLine {
        private String name;
        private String nameShort;
        private Vehicle vehicle;
    }

    @Getter
    @Setter
    @ToString
    public static class Vehicle {
        private String type;
    }

    @Getter
    @Setter
    @ToString
    public static class Polyline {
        private String encodedPolyline;
    }

    @Getter
    @Setter
    @ToString
    public static class LocationPoint {
        private LatLng latLng;
    }

    @Getter
    @Setter
    @ToString
    public static class LatLng {
        private Double latitude;
        private Double longitude;
    }

    public RouteInfo toRouteSummary() {
        if (routes == null || routes.isEmpty()) return null;
        Route route = routes.get(0);
        return new RouteInfo(route.getDistanceMeters(), parseDurationSeconds(route.getStaticDuration()), List.of());
    }

    public enum TravelMode {
        WALK(RouteInfo.TransportType.WALK),
        TRANSIT(RouteInfo.TransportType.TRAIN);

        private final RouteInfo.TransportType transportType;

        TravelMode(RouteInfo.TransportType transportType) {
            this.transportType = transportType;
        }

        public RouteInfo.TransportType toTransportType() {
            return transportType;
        }
    }

    public RouteInfo toRouteInfo() {
        if (routes == null || routes.isEmpty()) return null;
        Leg leg = routes.get(0).getLegs().get(0);

        List<RouteInfo.RouteStep> steps = leg.getSteps().stream()
                .map(step -> new RouteInfo.RouteStep(
                        toPoint(step.getStartLocation(), step.getTransitDetails() != null ? step.getTransitDetails().getStopDetails().getDepartureStop().getName() : null),
                        toPoint(step.getEndLocation(), step.getTransitDetails() != null ? step.getTransitDetails().getStopDetails().getArrivalStop().getName() : null),
                        TravelMode.valueOf(step.getTravelMode()).toTransportType(),
                        List.of()
                ))
                .collect(Collectors.toList());

        return new RouteInfo(leg.getDistanceMeters(), parseDurationSeconds(leg.getStaticDuration()), steps);
    }

    private static RouteInfo.Point toPoint(LocationPoint point, String name) {
        return new RouteInfo.Point( new Coordinate(point.getLatLng().getLatitude(), point.getLatLng().getLongitude()), name);
    }

    private static int parseDurationSeconds(String duration) {
        if (duration == null) return 0;
        return Integer.parseInt(duration.replace("s", ""));
    }
}
