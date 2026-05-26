package NoDam.Demo.place.dto.google;

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

    public RouteInfo toRouteInfo() {
        if (routes == null || routes.isEmpty()) return null;
        Leg leg = routes.get(0).getLegs().get(0);

        List<RouteInfo.RouteStep> steps = leg.getSteps().stream()
                .map(step -> {
                    RouteInfo.TransitInfo transitInfo = null;
                    if (step.getTransitDetails() != null) {
                        TransitDetails td = step.getTransitDetails();
                        transitInfo = new RouteInfo.TransitInfo(
                                td.getTransitLine().getName(),
                                td.getTransitLine().getNameShort(),
                                td.getTransitLine().getVehicle().getType(),
                                td.getStopDetails().getDepartureStop().getName(),
                                td.getStopDetails().getArrivalStop().getName(),
                                td.getStopCount()
                        );
                    }
                    return new RouteInfo.RouteStep(
                            toCoordinate(step.getStartLocation()),
                            toCoordinate(step.getEndLocation()),
                            step.getPolyline().getEncodedPolyline(),
                            step.getDistanceMeters(),
                            parseDurationSeconds(step.getStaticDuration()),
                            step.getTravelMode(),
                            transitInfo
                    );
                })
                .collect(Collectors.toList());

        return new RouteInfo(leg.getDistanceMeters(), parseDurationSeconds(leg.getStaticDuration()), steps);
    }

    private static Coordinate toCoordinate(LocationPoint point) {
        return new Coordinate(point.getLatLng().getLatitude(), point.getLatLng().getLongitude());
    }

    private static int parseDurationSeconds(String duration) {
        if (duration == null) return 0;
        return Integer.parseInt(duration.replace("s", ""));
    }
}
