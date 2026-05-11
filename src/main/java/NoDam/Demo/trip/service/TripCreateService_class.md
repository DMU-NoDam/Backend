# Class 책임
- 여행 생성 서비스 (Trip 도메인 생성, TripDate 생성)
# 함수
- public Trip createTrip(Long userId, TripCreateDto request)

TransactionTemplate과 DB Unique 제약 조건(uuid)을 이용해 멱등성을 보장하며 여행 일정 생성. 생성 실패 시(중복) 기존 항목 조회 후 반환.

- public List<TripDate> createTripDates(Trip trip, List<Region> regions, List<Place> selectedPlaceGoogleIds)

날짜 수만큼 TripDate 생성, region은 [0]만 배정. tripDates 존재 시 멱등성 처리.

# todo

## createTripDates
- selectedGooglePlace 일별 배정 로직 추가
- 일별 region 결정하는 로직 추가
