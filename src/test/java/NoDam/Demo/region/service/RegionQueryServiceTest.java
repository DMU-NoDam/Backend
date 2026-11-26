package NoDam.Demo.region.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.repository.RegionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/*
    RegionQueryService 단위 test (RegionRepository mock)

    repository는 요청 순서와 무관한 순서로 반환한다고 가정하고,
    service가 요청 순서대로 정렬하는지 / 없는 값이 섞이면 NOT_FOUND를 던지는지 확인한다
    tokyo(1, jp-tokyo, 도쿄), osaka(2, jp-osaka, 오사카)
 */
@ExtendWith(MockitoExtension.class)
class RegionQueryServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionQueryService regionQueryService;

    private final Region tokyo = Region.builder().id(1L).name("도쿄").code("jp-tokyo").build();
    private final Region osaka = Region.builder().id(2L).name("오사카").code("jp-osaka").build();

    @Nested
    @DisplayName("findRegionsByCode")
    class FindByCode {

        @Test
        @DisplayName("요청한 code 순서대로 정렬해서 반환한다")
        void sortByRequestOrder() {
            List<String> codes = List.of("jp-osaka", "jp-tokyo");
            when(regionRepository.findAllByCodeIn(codes)).thenReturn(List.of(tokyo, osaka));

            List<Region> result = regionQueryService.findRegionsByCode(codes);

            assertEquals(2, result.size());
            assertSame(osaka, result.get(0));
            assertSame(tokyo, result.get(1));
        }

        @Test
        @DisplayName("없는 code가 섞여 있으면 NOT_FOUND")
        void notFound() {
            List<String> codes = List.of("jp-tokyo", "jp-unknown");
            when(regionRepository.findAllByCodeIn(codes)).thenReturn(List.of(tokyo));

            CustomException exception = assertThrows(CustomException.class,
                    () -> regionQueryService.findRegionsByCode(codes));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }
    }

    @Nested
    @DisplayName("findRegionByName")
    class FindByName {

        @Test
        @DisplayName("요청한 이름 순서대로 정렬해서 반환한다")
        void sortByRequestOrder() {
            List<String> names = List.of("오사카", "도쿄");
            when(regionRepository.findAllByNameIn(names)).thenReturn(List.of(tokyo, osaka));

            List<Region> result = regionQueryService.findRegionByName(names);

            assertEquals(2, result.size());
            assertSame(osaka, result.get(0));
            assertSame(tokyo, result.get(1));
        }

        @Test
        @DisplayName("없는 이름이 섞여 있으면 NOT_FOUND")
        void notFound() {
            List<String> names = List.of("도쿄", "없는지역");
            when(regionRepository.findAllByNameIn(names)).thenReturn(List.of(tokyo));

            CustomException exception = assertThrows(CustomException.class,
                    () -> regionQueryService.findRegionByName(names));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }
    }

    @Nested
    @DisplayName("단건 / 전체 조회")
    class FindOne {

        @Test
        @DisplayName("findById : 있으면 region을 반환한다")
        void findById() {
            when(regionRepository.findById(1L)).thenReturn(Optional.of(tokyo));

            assertSame(tokyo, regionQueryService.findById(1L));
        }

        @Test
        @DisplayName("findById : 없으면 NOT_FOUND")
        void findByIdNotFound() {
            when(regionRepository.findById(99L)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> regionQueryService.findById(99L));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }

        @Test
        @DisplayName("findAll : repository 결과를 그대로 반환한다")
        void findAll() {
            List<Region> regions = List.of(tokyo, osaka);
            when(regionRepository.findAll()).thenReturn(regions);

            assertSame(regions, regionQueryService.findAll());
        }
    }
}
