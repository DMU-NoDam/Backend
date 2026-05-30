//package NoDam.Demo.conf;
//
//import NoDam.Demo.region.domain.Region;
//import NoDam.Demo.region.repository.RegionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitConfig {
//
//    private final RegionRepository regionRepository;
//
//    // super region (하위 region을 가짐)
//    private static final List<Object[]> SUPER_REGIONS = List.<Object[]>of(
//            new Object[]{"광동", "jp-non", 35.8825, 139.6398} // 간토 지방 지도 중심점
//    );
//
//    // sub region - superRegion 없음 (null)
//    private static final List<Object[]> STANDALONE_SUB_REGIONS = List.of(
//            new Object[]{"오사카", "jp-osaka", 34.6937, 135.5023},
//            new Object[]{"후쿠오카", "jp-fukuoka", 33.5902, 130.4017},
//            new Object[]{"삿포로", "jp-sapporo", 43.0618, 141.3545},
//            new Object[]{"오키나와", "OKI", 26.2124, 127.6792}
//    );
//
//    // sub region - superRegion 있음
//    private static final Map<String, List<Object[]>> CHILD_SUB_REGIONS = Map.of(
//            "광동", List.of(
//                    new Object[]{"도쿄", "jp-tokyo", 35.6895, 139.6917},
//                    new Object[]{"요코하마", "jp-yokohama", 35.4439, 139.6382},
//                    new Object[]{"가마쿠라", "jp-kamakura", 35.3197, 139.5525},
//                    new Object[]{"닛코", "jp-nikko", 36.7198, 139.6982},
//                    new Object[]{"하코네", "jp-hakone", 35.1894, 139.0247},
//                    new Object[]{"치바", "jp-chiba", 35.6073, 140.1064},
//                    new Object[]{"사이타마", "jp-saitama", 35.8857, 139.6682}
//            )
//    );
//
//    @Bean
//    @Transactional
//    public CommandLineRunner initData() {
//        return args -> {
//            // 1. super region 저장
//            Map<String, Region> savedSuperRegions = new HashMap<>();
//            for (Object[] data : SUPER_REGIONS) {
//                String name = (String) data[0];
//                String code = (String) data[1];
//                Double lat = (Double) data[2];
//                Double lon = (Double) data[3];
//
//                Region region = regionRepository.findFirstByName(name)
//                        .orElseGet(() -> Region.builder().name(name).code(code).build());
//                region.setLat(lat);
//                region.setLon(lon);
//                savedSuperRegions.put(name, regionRepository.save(region));
//            }
//
//            // 2. standalone sub region 저장 (superRegion = null)
//            for (Object[] data : STANDALONE_SUB_REGIONS) {
//                String name = (String) data[0];
//                String code = (String) data[1];
//                Double lat = (Double) data[2];
//                Double lon = (Double) data[3];
//
//                Region region = regionRepository.findFirstByName(name)
//                        .orElseGet(() -> Region.builder().name(name).code(code).build());
//                region.setLat(lat);
//                region.setLon(lon);
//                region.setSuperRegion(null);
//                regionRepository.save(region);
//            }
//
//            // 3. child sub region 저장 (superRegion 연결)
//            for (Map.Entry<String, List<Object[]>> entry : CHILD_SUB_REGIONS.entrySet()) {
//                Region parent = savedSuperRegions.get(entry.getKey());
//                for (Object[] data : entry.getValue()) {
//                    String name = (String) data[0];
//                    String code = (String) data[1];
//                    Double lat = (Double) data[2];
//                    Double lon = (Double) data[3];
//
//                    Region region = regionRepository.findFirstByName(name)
//                            .orElseGet(() -> Region.builder().name(name).code(code).build());
//                    region.setLat(lat);
//                    region.setLon(lon);
//                    region.setSuperRegion(parent);
//                    regionRepository.save(region);
//                }
//            }
//
//            log.info("Region initialization completed.");
//        };
//    }
//}
