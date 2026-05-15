package NoDam.Demo.region.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.util.ListUtil;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionQueryService {

    private final RegionRepository regionRepository;

    public List<Region> findRegionsByCode(List<String> regionCodes) {
        List<Region> regions = regionRepository.findAllByCodeIn(regionCodes);
        List<Region> sortedRegions = ListUtil.sortByRequestOrder(regionCodes, regions, Region::getCode);

        if (sortedRegions.contains(null)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        return sortedRegions;
    }

    public List<Region> findRegionByName(List<String> regionNames) {
        List<Region> regions = regionRepository.findAllByNameIn(regionNames);
        List<Region> sortedRegions = ListUtil.sortByRequestOrder(regionNames, regions, Region::getName);

        if (sortedRegions.contains(null)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        return sortedRegions;
    }

    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    public Region findById(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    // input 좌표를 포함하는 region을 찾음 (구현 전!)
    public Region findByCoordinate(double lat, double lon) {
        // region별 좌표 구역이 필요함, native query 필요함
        return regionRepository.findById(1L).get();
    }

}
