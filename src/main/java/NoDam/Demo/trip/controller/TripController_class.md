# Class 책임
- 여행 일정 관련 API 엔드포인트 제공
# 함수
- ResponseEntity createTrip(User user, @Valid TripCreateFacadeRequestDto request) : 여행 일정 생성 API
- ResponseEntity getTripList(User user) : 여행 일정 목록 조회 API
- ResponseEntity getTodayTrip(User user) : 오늘 고정된 여행 조회 API (없으면 null 반환)
- ResponseEntity updateTripFixed(User user, Long tripId, boolean isFixed) : 여행 고정 여부 수정 API
# todo
- trip theme 선택 api 필요
- - trip auto 생성 이후 trip theme 별 일정을 반환하고, 사용자가 선택할 api 필요함