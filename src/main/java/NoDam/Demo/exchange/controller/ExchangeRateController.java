package NoDam.Demo.exchange.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.exchange.dto.ExchangeRateResponseDto;
import NoDam.Demo.exchange.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Exchange", description = "환율 정보 관련 API")
@RestController
@RequestMapping("/exchange/public")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    // 예: GET /exchange/public/rate?base=KRW&target=JPY → 1 KRW = rate JPY
    @Operation(summary = "환율 조회", description = "기준 통화 1단위가 대상 통화로 얼마인지 조회합니다. 통화 쌍마다 1시간 캐시됩니다.")
    @GetMapping("/rate")
    public ResponseEntity<SuccessResponse<ExchangeRateResponseDto>> getRate(
            @RequestParam(defaultValue = "KRW") String base,
            @RequestParam String target
    ) {
        return ResponseEntity.ok().body(new SuccessResponse<>("success", exchangeRateService.getRate(base, target)));
    }
}
