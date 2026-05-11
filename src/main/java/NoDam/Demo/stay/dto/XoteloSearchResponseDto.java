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

        public XoteloSearchResponseDto toDto() {
            return XoteloSearchResponseDto.builder()
                    .hotelKey(this.hotelKey)
                    .name(this.name)
                    .address(this.streetAddress)
                    .image(this.image)
                    .url(this.url)
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
