package NoDam.Demo.plan.service;

import NoDam.Demo.place.domain.Place;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NecessaryPlaceDistributeService {

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
