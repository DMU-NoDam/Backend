package NoDam.Demo.common.type;

public enum PlaceType {

    RESTAURANT("식당"), // 식당
    CAFE("카페"), // 카페 (추가됨 05/13)
    SIGHT("관광지"), // 관광지
    SHOP("쇼핑"), // 쇼핑
    HOTEL("호텔"),
    AIRPORT("공항"); // 공항

    private final String name;

    PlaceType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
