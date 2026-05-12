package NoDam.Demo.conf;

import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig {

    private final RegionRepository regionRepository;

    private static final List<Object[]> DEFAULT_REGIONS = List.of(
        new Object[]{"도쿄", "TYO", 35.6895, 139.6917},
        new Object[]{"오사카", "OSA", 34.6937, 135.5023},
        new Object[]{"후쿠오카", "FUK", 33.5902, 130.4017},
        new Object[]{"삿포로", "SPK", 43.0618, 141.3545},
        new Object[]{"교토", "KYO", 35.0116, 135.7681},
        new Object[]{"오키나와", "OKI", 26.2124, 127.6792}
    );

    @Bean
    @Transactional
    public CommandLineRunner initData() {
        return args -> {
            for (Object[] data : DEFAULT_REGIONS) {
                String name = (String) data[0];
                String code = (String) data[1];
                Double lat = (Double) data[2];
                Double lon = (Double) data[3];

                // 이름으로 조회하여 없으면 생성, 있으면 좌표 업데이트
                Region region = regionRepository.findFirstByName(name)
                        .orElseGet(() -> Region.builder().name(name).code(code).build());
                
                region.setLat(lat);
                region.setLon(lon);
                regionRepository.save(region);
            }
            log.info("Region coordinates initialization completed.");
        };
    }
}
