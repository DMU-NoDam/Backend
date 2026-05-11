package NoDam.Demo.region.dto.response;

import NoDam.Demo.region.domain.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegionResponseDto {
    private String name;
    private String code;

    public static RegionResponseDto from(Region region) {
        return new RegionResponseDto(region.getName(), region.getCode());
    }
}
