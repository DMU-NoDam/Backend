package NoDam.Demo.stay.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.stay.dto.XoteloRatesResponseDto;
import NoDam.Demo.stay.dto.XoteloSearchResponseDto;
import NoDam.Demo.stay.service.XoteloRatesService;
import NoDam.Demo.stay.service.XoteloSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stay/public")
@Tag(name = "StayController")
public class StayController {

    private final XoteloSearchService xoteloSearchService;
    private final XoteloRatesService xoteloRatesService; // 추가 : /rates 호출용

    @GetMapping("/search")
    @Operation(summary = "stay 지역 코드로 검색")
    public ResponseEntity<SuccessResponse<List<XoteloSearchResponseDto>>> searchStays(@RequestParam String regionCode) {
        List<XoteloSearchResponseDto> stays = xoteloSearchService.searchStays(regionCode);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", stays));
    }

    // 추가 : hotelKey + 체크인/체크아웃 으로 OTA 별 가격 조회
    // 예: GET /stay/public/rates?hotelKey=g14129528-d310308&checkIn=2026-05-18&checkOut=2026-05-22
    @GetMapping("/rates")
    @Operation(summary = "stay OTA 별 가격 조회")
    public ResponseEntity<SuccessResponse<List<XoteloRatesResponseDto>>> getStayRates(
            @RequestParam String hotelKey,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        List<XoteloRatesResponseDto> rates = xoteloRatesService.getRates(hotelKey, checkIn, checkOut);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", rates));
    }
}
