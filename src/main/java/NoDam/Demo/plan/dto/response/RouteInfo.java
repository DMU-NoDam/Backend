package NoDam.Demo.plan.dto.response;

import NoDam.Demo.place.domain.Coordinate;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RouteInfo {

    private Integer totalDistanceMeters;
    private Integer totalDurationSeconds;
    private List<RouteStep> steps;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class RouteStep {
        private Point start;
        private Point end;
        private TransportType methodType;
        private List<Point> polygon;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Point {
        private Coordinate coordinate;
        private String name;
        // private PointType pointType; // station, start, end 일단 보류
    }

    public enum TransportType {
        WALK ("도보"),
        TRAIN ("열차"), // 열차 맞나? 지하철?
        ;

        private String name;

        TransportType(String name) { this.name = name; }
        public String toString() { return name; }
    }

    public static RouteInfo empty() {
        return new RouteInfo(null, null, List.of());
    }

    public void setStartAndEndName(String startName, String endName) {
        steps.getFirst().getStart().name = startName;
        steps.getLast().getEnd().name = endName;
    }

}
