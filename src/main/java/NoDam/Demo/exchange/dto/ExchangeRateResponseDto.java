package NoDam.Demo.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExchangeRateResponseDto {
    private String base;    // 기준 통화 (예: KRW)
    private String target;  // 대상 통화 (예: JPY)
    private double rate;    // 1 base = rate target
    private long updatedAt; // 외부 API에서 환율을 받아온 시각 (epoch millis, 서버 타임존과 무관)
}
