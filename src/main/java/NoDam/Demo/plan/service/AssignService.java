package NoDam.Demo.plan.service;

import NoDam.Demo.adapter.ai.AiPort;
import NoDam.Demo.adapter.ai.Prompt;
import NoDam.Demo.common.util.TimeUtil;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.domain.AirportSchedule;
import NoDam.Demo.plan.dto.ai.AiRegionAssignRequestDto;
import NoDam.Demo.plan.dto.ai.AiRegionAssignResponseDto;
import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignService {

    private final AiPort aiPort;
    private final boolean isMockAi;

    private static final LocalTime DEFAULT_ARRIVAL_TIME = LocalTime.of(10, 0); // 항공편 미입력 시 도착 기준 시간
    private static final LocalTime DEFAULT_DEPART_TIME = LocalTime.of(18, 0);  // 항공편 미입력 시 출발 기준 시간

    // TODO: 공항 region 강제화 - 1일차=공항 region 고정, 마지막날은 다른 region 최소 1일 확보 시에만 고정
    // TODO: 입력 검증 추가 - 공항 region ∈ 선택 region 불변식 (현재 대조 없음)
    // TODO: AI에는 순서 말고 분배(각 region 며칠)만 위임
    // todo : 현재 ai의존적임, depart airport정보가 있음에도 사용되지 않음, 로직상 분배해도 문제 없어 보임
    public Map<LocalDate, Region> assignRegion(
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

    // 날짜별 호텔 배정 (plan 규칙 소유)
    // - 마지막 날은 체크아웃이므로 호텔 배정 안 함 (결과에서 제외)
    // - 사용자 지정 호텔이 있으면 전 날짜 동일, 없으면 각 날짜 region의 추천 호텔
    // - 추천/변환은 하지 않음. 이미 Place로 변환된 추천 호텔을 입력으로 받는다
    public Map<LocalDate, Place> assignHotel(
            List<LocalDate> dates,
            Map<LocalDate, Region> dateRegionMap,
            Map<Region, Place> recommendedHotelByRegion,
            Optional<Place> userHotel
    ) {
        Map<LocalDate, Place> result = new LinkedHashMap<>();
        if (dates == null || dates.isEmpty()) return result;

        LocalDate checkoutDate = dates.get(dates.size() - 1); // 마지막 날 = 체크아웃

        for (LocalDate date : dates) {
            if (date.equals(checkoutDate)) continue; // 마지막 날: 호텔 없음

            Place hotel = userHotel
                    .orElseGet(() -> recommendedHotelByRegion.get(dateRegionMap.get(date)));
            if (hotel != null) result.put(date, hotel);
        }

        return result;
    }

    // 공항 배정 (plan 규칙 소유)
    // - 첫날 = 도착 공항, 마지막날 = 출발 공항
    // - 사용자 항공편 있으면 그 공항 + 항공편 시간 정시 올림, 없으면 추천 공항 + 기본 시간(도착 10:00 / 출발 18:00)
    // - 공항 선택(추천)은 하지 않음. 사용자 항공편(Optional)과 이미 resolved된 추천 공항을 입력으로 받는다
    public Map<LocalDate, AirportSchedule> assignAirport(
            LocalDate startDate,
            LocalDate endDate,
            Optional<AirportSchedule> arrivalFlight,
            Optional<AirportSchedule> departFlight,
            Place recommendArrival,
            Place recommendDepart
    ) {
        Map<LocalDate, AirportSchedule> result = new LinkedHashMap<>();

        result.put(startDate, arrivalFlight
                .map(flight -> new AirportSchedule(flight.airport(), TimeUtil.ceilToNextHour(flight.time())))
                .orElse(new AirportSchedule(recommendArrival, DEFAULT_ARRIVAL_TIME)));

        // 첫날 == 마지막날(당일치기)이면 도착 배정을 유지한다
        result.putIfAbsent(endDate, departFlight
                .map(flight -> new AirportSchedule(flight.airport(), TimeUtil.ceilToNextHour(flight.time())))
                .orElse(new AirportSchedule(recommendDepart, DEFAULT_DEPART_TIME)));

        return result;
    }

    // TODO: Nearest Neighbor 클러스터링 + 날짜별 가용 시간 매칭으로 고도화
    public Map<LocalDate, List<Place>> distribute(List<Place> necessaryPlaces, List<LocalDate> dates) {
        Map<LocalDate, List<Place>> result = new LinkedHashMap<>();
        dates.forEach(d -> result.put(d, new ArrayList<>()));

        if (necessaryPlaces == null || necessaryPlaces.isEmpty()) return result;

        int total = necessaryPlaces.size();
        int days = dates.size();
        int perDay = total / days;
        int remainder = total % days;

        int cursor = 0;
        for (int i = 0; i < days; i++) {
            int count = perDay + (i < remainder ? 1 : 0);
            result.get(dates.get(i)).addAll(necessaryPlaces.subList(cursor, cursor + count));
            cursor += count;
        }

        return result;
    }

}
