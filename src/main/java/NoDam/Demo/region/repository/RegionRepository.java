package NoDam.Demo.region.repository;

import NoDam.Demo.region.domain.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByCode(String code);

    Optional<Region> findFirstByName(String name);

    List<Region> findAllByCodeIn(List<String> codes);

    List<Region> findAllByNameIn(List<String> names);
}
