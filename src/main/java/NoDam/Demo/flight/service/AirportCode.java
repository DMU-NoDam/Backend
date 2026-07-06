package NoDam.Demo.flight.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;

// place_id 값은 init.sql의 공항 place insert id와 일치해야 함
public enum AirportCode {

    // 한국 (1~9)
    ICN(1L,  "인천국제공항"),
    GMP(2L,  "김포국제공항"),
    PUS(3L,  "김해국제공항"),
    CJU(4L,  "제주국제공항"),
    TAE(5L,  "대구국제공항"),
    CJJ(6L,  "청주국제공항"),

    // 일본 - 도쿄 (10~)
    NRT(10L, "나리타국제공항"),
    HND(11L, "하네다공항"),

    // 일본 - 오사카
    KIX(12L, "간사이국제공항"),

    // 일본 - 나고야
    NGO(13L, "주부국제공항"),

    // 일본 - 후쿠오카
    FUK(14L, "후쿠오카공항"),

    // 일본 - 삿포로
    CTS(15L, "신치토세공항"),

    // 일본 - 오키나와
    OKA(16L, "나하공항"),

    // 일본 - 기타
    HIJ(17L, "히로시마공항"),
    SDJ(18L, "센다이공항");

    private final Long placeId;
    private final String name;

    AirportCode(Long placeId, String name) {
        this.placeId = placeId;
        this.name = name;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public static AirportCode from(String iataCode) {
        try {
            return AirportCode.valueOf(iataCode.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
    }
}
