package NoDam.Demo.stay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XoteloSearchResponseDto {

    private String hotelKey;
    private String name;
    private String address;
    private String image;
    private String url;

    // 추가 : 추후 위치 기반 추천 / 지도 표시용 좌표
    private Double latitude;
    private Double longitude;

    @Getter
    @NoArgsConstructor
    public static class RawResult {
        @JsonProperty("hotel_key")
        private String hotelKey;
        private String name;
        @JsonProperty("street_address")
        private String streetAddress;
        private String image;
        private String url;

        // 추가 : Xotelo 응답의 좌표 매핑
        private Double latitude;
        private Double longitude;

        public XoteloSearchResponseDto toDto() {
            return XoteloSearchResponseDto.builder()
                    .hotelKey(this.hotelKey)
                    .name(this.name)
                    .address(this.streetAddress)
                    .image(this.image)
                    .url(this.url)
                    .latitude(this.latitude)
                    .longitude(this.longitude)
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
            private String query;
            private List<RawResult> list; // 실제 데이터는 list 필드에 있음
        }
    }
}
