# Class 책임
- 장소 정보 엔티티
- 최고 하위 region_id를 가져야 함
# 함수
- public Place(Long regionId, PlaceType placeType, String googleId, String name, String address, Double x, Double y, WeatherType recommendWeatherType, TripThemeType recommendTripThemeType, SeasonType recommendSeasonType, PriceType priceType) : 빌더를 통한 생성
# todo
- google 정보 처리 
- - 단순 google id 만 충분한지 확인

- placeType 별 place class 분리
- - 충분히 고려하고 진행해야함
- - 어떤 place인지 코드에서 확인 가능, type 안전성 챙길 수 있음