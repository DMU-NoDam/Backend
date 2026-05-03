# Class 책임
- 여행 일정 관련 비즈니스 로직 처리
# 함수
- Trip createTrip(Long userId, CreateTripRequest request) : DB Unique 제약 조건과 try-catch를 이용해 멱등성을 보장하며 여행 일정 생성
- List<Trip> getTripList(Long userId) : 사용자의 여행 일정 목록 반환
# todo
- 없음
