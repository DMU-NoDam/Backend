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
@Transactional(readOnly = true)
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

}
