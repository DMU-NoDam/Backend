package NoDam.Demo.conf;

import NoDam.Demo.common.type.PlaceType;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.region.repository.RegionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "init-data-check", havingValue = "true", matchIfMissing = false)
public class InitDataChecker implements ApplicationRunner {

    private static final String INIT_SQL = "insert_places.sql";

    private final RegionRepository regionRepository;
    private final PlaceRepository placeRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<String> missing = new ArrayList<>();

        long regionCount = regionRepository.count();
        if (regionCount == 0) {
            missing.add("region 테이블이 비어 있음");
        }

        long placeCount = placeRepository.count();
        if (placeCount == 0) {
            missing.add("place 테이블이 비어 있음");
        }

        if (missing.isEmpty()) {
            log.info("초기 데이터 검사 통과 (region = {}, place = {})",
                    regionCount, placeCount);
            return;
        }

        missing.forEach(reason -> log.error("초기 데이터 누락 : {}", reason));
        log.error("{} 를 DB에 실행한 뒤 다시 기동하세요.", INIT_SQL);

        throw new IllegalStateException(
                "초기 데이터가 적재되지 않아 기동을 중단합니다. "
                        + INIT_SQL + " 실행 후 재기동하세요. 누락 : " + missing);
    }
}
