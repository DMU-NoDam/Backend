package NoDam.Demo.stay.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;

import java.util.Arrays;
import java.util.Optional;

public enum XoteloRegionCode {

    TOKYO("jp-tokyo", "g298184"),
    OSAKA("jp-osaka", "g298566"),
    FUKUOKA("jp-fukuoka", "g298207"),
    ;

    private final String regionCode;
    private final String xoteloCode;

    XoteloRegionCode(String regionCode, String xoteloCode) {
        this.regionCode = regionCode;
        this.xoteloCode = xoteloCode;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getXoteloCode() {
        return xoteloCode;
    }

    public static XoteloRegionCode getByRegionCode(String regionCode) {
        return Arrays.stream(values())
                .filter(e -> e.regionCode.equals(regionCode))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }
}
