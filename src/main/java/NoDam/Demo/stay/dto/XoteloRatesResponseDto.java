package NoDam.Demo.stay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Xotelo /api/rates 응답 DTO
 * - 클라이언트 응답 : { otaName, rate, currency }
 * - 외부 응답 매핑용 내부 클래스 : RawResponse / ResultContainer / RawRate
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XoteloRatesResponseDto {

    private String otaName;     // 예: "Booking.com", "Expedia"
    private Double rate;        // 1박 가격 (응답 통화 기준)
    private String currency;    // 통화 (Xotelo 기본 USD)

    @Getter
    @NoArgsConstructor
    public static class RawRate {
        @JsonProperty("code")
        private String code;            // OTA 식별 코드 (예: "BOOKING", "EXPEDIA")

        @JsonProperty("name")
        private String name;            // OTA 이름

        @JsonProperty("rate")
        private Double rate;            // 1박 평균 가격

        @JsonProperty("tax")
        private Double tax;             // 세금/수수료

        @JsonProperty("total")
        private Double total;           // 총 금액

        public XoteloRatesResponseDto toDto(String currency) {
            return XoteloRatesResponseDto.builder()
                    .otaName(this.name != null ? this.name : this.code)
                    .rate(this.rate)
                    .currency(currency)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class RawResponse {
        private ResultContainer result;

        @Getter
        @NoArgsConstructor
        public static class ResultContainer {
            @JsonProperty("hotel_key")
            private String hotelKey;

            @JsonProperty("chk_in")
            private String chkIn;

            @JsonProperty("chk_out")
            private String chkOut;

            private String currency;

            private List<RawRate> rates;
        }
    }
}
