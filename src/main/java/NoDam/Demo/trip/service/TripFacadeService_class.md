# Class 책임
- Trip 관련 facade 서비스 (TripCreateService, TripUpdateService, TripSelectService, AutoCreatePlanService 조율)
# 함수
- public Trip createTrip(Long userId, TripCreateFacadeRequestDto request)
- public List<Trip> getTripList(Long userId)
- public Optional<Trip> getTodayTrip(Long userId)
- public Trip updateTripFixed(Long userId, Long tripId, boolean isFixed)
# todo
- 공항 plan 생성
- 숙소 plan 생성
- selectedPlace 연동 (placeSelectService)
- 비동기 일정 생성 시작 전, Trip의 상태를 PLANNING으로 변경하는 로직을 Facade의 동기 로직으로 이동
- - 이유: @Async 메서드 내부에서 상태를 바꾸면, 비동기 스레드 실행 전 컨트롤러 응답이 나가버려 사용자에게 '생성 중 아님(isPlanning: false)' 상태가 잘못 전달될 수 있음
- - 흐름: [동기] 상태를 PLANNING으로 변경 -> [비동기] 일정 생성 시작 -> [비동기 종료] 상태를 PLANNED로 변경

