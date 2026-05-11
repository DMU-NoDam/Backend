package NoDam.Demo.conf;

import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.repository.RegionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig {

    private final RegionRepository regionRepository;

    // 기본 지역 데이터 정의
    private static final List<String[]> DEFAULT_REGIONS = List.of(
        new String[]{"도쿄", "TYO"},
        new String[]{"오사카", "OSA"},
        new String[]{"후쿠오카", "FUK"},
        new String[]{"삿포로", "SPK"},
        new String[]{"교토", "KYO"},
        new String[]{"오키나와", "OKI"}
    );

    @PostConstruct
    public void init() {
        initRegions();
    }

    private void initRegions() {
        DEFAULT_REGIONS.forEach(data -> saveRegionSafely(data[0], data[1]));
    }

    private void saveRegionSafely(String name, String code) {
        try {
            if (regionRepository.findByCode(code).isEmpty()) {
                regionRepository.save(Region.builder()
                        .name(name)
                        .code(code)
                        .build());
                log.info("Region initialized: {} ({})", name, code);
            }
        } catch (Exception e) {
            log.error("Failed to initialize region: {} ({}) - {}", name, code, e.getMessage());
        }
    }
}
