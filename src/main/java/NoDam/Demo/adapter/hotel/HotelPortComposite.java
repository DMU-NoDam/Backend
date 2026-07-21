package NoDam.Demo.adapter.hotel;

import NoDam.Demo.region.domain.Region;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

// HotelPort 구현체들을 순서대로 시도하는 공통 adapter
// 외부 api가 결과를 못 주거나 예외를 던지면 db fallback으로 넘어간다
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class HotelPortComposite implements HotelPort {

    private final XoteloHotelAdapter xoteloHotelAdapter;
    private final DbHotelAdapter dbHotelAdapter;

    @Override
    public Optional<String> recommendHotelGoogleId(Region region) {
        try {
            Optional<String> googleId = xoteloHotelAdapter.recommendHotelGoogleId(region);
            if (googleId.isPresent()) return googleId;

            log.warn("호텔 추천 결과 없음 adapter=XoteloHotelAdapter, regionCode={}", region.getCode());
        } catch (Exception e) {
            log.warn("호텔 추천 실패 adapter=XoteloHotelAdapter, regionCode={}", region.getCode(), e);
        }

        return dbHotelAdapter.recommendHotelGoogleId(region); // fallback
    }
}
