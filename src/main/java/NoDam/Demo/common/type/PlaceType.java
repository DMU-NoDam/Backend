package NoDam.Demo.common.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PlaceType {

    RESTAURANT("식당"), // 식당
    CAFE("카페"), // 카페 (추가됨 05/13)
    SIGHT("관광지"), // 관광지
    SHOP("쇼핑"), // 쇼핑
    HOTEL("호텔"),
    AIRPORT("공항"); // 공항

    private final String korean;

    PlaceType(String korean) {
        this.korean = korean;
    }

    @Override
    public String toString() {
        return korean;
    }

    @JsonCreator
    public static PlaceType fromKorean(String value) {
        return Arrays.stream(values())
                .filter(e -> e.korean.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown PlaceType: " + value));
    }

    @JsonValue
    public String getKorean() {
        return korean;
    }

}
