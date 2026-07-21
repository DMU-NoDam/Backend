package NoDam.Demo.adapter.hotel;

import NoDam.Demo.region.domain.Region;

import java.util.Optional;

// 지역별 호텔 추천 output port
public interface HotelPort {

    // 추천 호텔의 google place id 반환, 찾지 못하면 Optional.empty()
    Optional<String> recommendHotelGoogleId(Region region);

}
