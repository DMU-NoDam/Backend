package NoDam.Demo.adapter.route.dto;

import NoDam.Demo.place.domain.Coordinate;
import NoDam.Demo.plan.dto.response.RouteInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NavitimeRouteResponseDto {

    private List<Item> items;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Summary summary;
        private List<Section> sections;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        private Move move;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Move {
        @JsonProperty("walk_distance")
        private Integer walkDistance;
        private Integer time;
        private Integer distance;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private String type;    // "point" or "move"
        private String move;    // "walk", "rapid_train", etc.
        private Coord coord;
        private String name;
        @JsonProperty("node_types")
        private List<String> nodeTypes;
        private Integer time;
        private Integer distance;
        @JsonProperty("line_name")
        private String lineName;
        private Transport transport;

        public boolean isStation() {
            return nodeTypes != null && nodeTypes.contains("station");
        }

        public boolean isWalk() {
            return "walk".equals(move);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coord {
        private Double lat;
        private Double lon;

        public Coordinate toCoordinate() {
            return new Coordinate(lat, lon);
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transport {
        private String color;
        @JsonProperty("calling_at")
        private List<CallingAt> callingAt;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallingAt {
        private String name;
        private Coord coord;
    }

    public RouteInfo toRouteInfo() {
        if (items == null || items.isEmpty()) return null;
        Item item = items.get(0);

        Move move = item.getSummary().getMove();
        List<Section> sections = item.getSections();

        List<RouteInfo.RouteStep> steps = new java.util.ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            if (!"move".equals(section.getType())) continue;

            RouteInfo.Point start = findNearestPoint(sections, i, -1);
            RouteInfo.Point end = findNearestPoint(sections, i, 1);
            RouteInfo.TransportType transportType = section.isWalk()
                    ? RouteInfo.TransportType.WALK
                    : RouteInfo.TransportType.TRAIN;

            List<RouteInfo.Point> callingAtCoords = section.getTransport() != null && section.getTransport().getCallingAt() != null
                    ? section.getTransport().getCallingAt().stream()
                        .filter(c -> c.getCoord() != null)
                        .map(c -> new RouteInfo.Point(c.getCoord().toCoordinate(), c.getName()))
                        .toList()
                    : List.of();

            steps.add(new RouteInfo.RouteStep(start, end, transportType, callingAtCoords));
        }

        return new RouteInfo(move.getDistance(), move.getTime() * 60, steps);
    }

    private RouteInfo.Point findNearestPoint(List<Section> sections, int from, int direction) {
        for (int i = from + direction; i >= 0 && i < sections.size(); i += direction) {
            Section s = sections.get(i);
            if ("point".equals(s.getType()) && s.getCoord() != null) {
                return new RouteInfo.Point(s.getCoord().toCoordinate(), s.getName());
            }
        }
        return null;
    }
}
