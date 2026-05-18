package NoDam.Demo.plan.dto.response;

import NoDam.Demo.place.domain.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfo {

    private Integer totalDistanceMeters;
    private Integer totalDurationSeconds;
    private List<RouteStep> steps;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteStep {
        private Coordinate start;
        private Coordinate end;
        private String encodedPolyline;
        private Integer distanceMeters;
        private Integer durationSeconds;
        private String travelMode;
        private TransitInfo transitInfo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransitInfo {
        private String lineName;
        private String lineShortName;
        private String vehicleType;
        private String departureStopName;
        private String arrivalStopName;
        private Integer stopCount;
    }

    public static RouteInfo empty() {
        return new RouteInfo(null, null, List.of());
    }

    public RouteInfoResponse toResponse() {
        List<RouteInfoResponse.RouteStepResponse> stepResponses = steps.stream()
                .map(step -> new RouteInfoResponse.RouteStepResponse(
                        step.getStart(),
                        step.getEnd(),
                        decodePolyline(step.getEncodedPolyline()),
                        step.getDistanceMeters(),
                        step.getDurationSeconds(),
                        step.getTravelMode(),
                        step.getTransitInfo()
                ))
                .collect(Collectors.toList());

        return new RouteInfoResponse(totalDistanceMeters, totalDurationSeconds, stepResponses);
    }

    private static List<Coordinate> decodePolyline(String encoded) {
        List<Coordinate> result = new ArrayList<>();
        if (encoded == null) return result;

        int index = 0, lat = 0, lng = 0;
        while (index < encoded.length()) {
            int b, shift = 0, value = 0;
            do {
                b = encoded.charAt(index++) - 63;
                value |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lat += ((value & 1) != 0) ? ~(value >> 1) : (value >> 1);

            shift = 0; value = 0;
            do {
                b = encoded.charAt(index++) - 63;
                value |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lng += ((value & 1) != 0) ? ~(value >> 1) : (value >> 1);

            result.add(new Coordinate(lat / 1e5, lng / 1e5));
        }
        return result;
    }
}
