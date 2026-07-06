package NoDam.Demo.plan.service;

import NoDam.Demo.adapter.ai.AiPort;
import NoDam.Demo.adapter.ai.Prompt;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.dto.ai.AiRegionAssignRequestDto;
import NoDam.Demo.plan.dto.ai.AiRegionAssignResponseDto;
import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionAssignService {

    private final AiPort aiPort;
    private final boolean isMockAi;

    // TODO: 공항 region 강제화 - 1일차=공항 region 고정, 마지막날은 다른 region 최소 1일 확보 시에만 고정
    // TODO: 입력 검증 추가 - 공항 region ∈ 선택 region 불변식 (현재 대조 없음)
    // TODO: AI에는 순서 말고 분배(각 region 며칠)만 위임
    // todo : 현재 ai의존적임, depart airport정보가 있음에도 사용되지 않음, 로직상 분배해도 문제 없어 보임
    public Map<LocalDate, Region> assign(
            List<LocalDate> dates,
            List<Region> regions,
            List<Place> necessaryPlaces,
            Optional<Place> arrivalAirport,
            Optional<Place> departAirport // 출발 (귀국) 공항
    ) {
        if (regions.size() == 1) {
            return dates.stream().collect(Collectors.toMap(d -> d, d -> regions.get(0),
                    (a, b) -> a, LinkedHashMap::new));
        }

        if(isMockAi)
            return mockAssign(dates, regions, necessaryPlaces);

        AiRegionAssignRequestDto request = buildRequest(dates, regions, necessaryPlaces, arrivalAirport);
        AiRegionAssignResponseDto response = aiPort.call(Prompt.ASSIGN_REGION, AiRegionAssignResponseDto.class, request);

        Map<LocalDate, Region> result = parseResponse(response, regions, dates);
        return result;
    }

    private Map<LocalDate, Region> parseResponse(AiRegionAssignResponseDto response, List<Region> regions, List<LocalDate> dates) {
        Map<Long, Region> regionMap = regions.stream().collect(Collectors.toMap(Region::getId, r -> r));
        Map<LocalDate, Region> result = new LinkedHashMap<>();

        for (AiRegionAssignResponseDto.DateRegionMapping mapping : response.getAssignments()) {
            try {
                LocalDate date = LocalDate.parse(mapping.getDate());
                Region region = regionMap.get(mapping.getRegionId());
                if (date != null && region != null) result.put(date, region);
            } catch (Exception ignored) {}
        }

        return result;
    }

    // mock: 필수 장소 수 비율로 날짜 배분, region[0] 앞 날짜 / region[1] 뒷 날짜
    private Map<LocalDate, Region> mockAssign(List<LocalDate> dates, List<Region> regions, List<Place> necessaryPlaces) {
        Map<LocalDate, Region> result = new LinkedHashMap<>();
        int totalDays = dates.size();

        long region0Count = necessaryPlaces.stream()
                .filter(p -> p.getRegionId().equals(regions.get(0).getId()))
                .count();
        long totalPlaces = necessaryPlaces.size();

        int region0Days;
        if (totalPlaces == 0) {
            region0Days = Math.max(1, totalDays / 2);
        } else {
            region0Days = (int) Math.round((double) region0Count / totalPlaces * totalDays);
            region0Days = Math.max(1, Math.min(region0Days, totalDays - 1));
        }

        for (int i = 0; i < totalDays; i++) {
            result.put(dates.get(i), i < region0Days ? regions.get(0) : regions.get(1));
        }

        return result;
    }

    private AiRegionAssignRequestDto buildRequest(
            List<LocalDate> dates, List<Region> regions, List<Place> necessaryPlaces,
            Optional<Place> airport
    ) {
        Map<Long, Long> placeCountByRegion = necessaryPlaces.stream()
                .collect(Collectors.groupingBy(Place::getRegionId, Collectors.counting()));

        List<AiRegionAssignRequestDto.RegionInfo> regionInfos = regions.stream()
                .map(r -> AiRegionAssignRequestDto.RegionInfo.builder()
                        .regionId(r.getId())
                        .name(r.getName())
                        .lat(r.getLat())
                        .lon(r.getLon())
                        .placeCount(placeCountByRegion.getOrDefault(r.getId(), 0L).intValue())
                        .build())
                .toList();

        List<AiRegionAssignRequestDto.PlaceCoordinate> placeCoords = necessaryPlaces.stream()
                .map(p -> AiRegionAssignRequestDto.PlaceCoordinate.builder()
                        .placeId(p.getId()).name(p.getName()).lat(p.getLat()).lon(p.getLon())
                        .build())
                .toList();

        AiRegionAssignRequestDto.PlaceCoordinate airportCoord = airport.isEmpty() ? null :
                AiRegionAssignRequestDto.PlaceCoordinate.builder()
                        .placeId(airport.get().getId()).name(airport.get().getName())
                        .lat(airport.get().getLat()).lon(airport.get().getLon()).build();

        return AiRegionAssignRequestDto.builder()
                .dates(dates.stream().map(LocalDate::toString).toList())
                .regions(regionInfos)
                .necessaryPlaces(placeCoords)
                .airport(airportCoord)
                .build();
    }

}
