# Class 책임
- 여행 일정 관련 비즈니스 로직 처리
# 함수
- Trip createTrip(Long userId, CreateTripRequest request) : TransactionTemplate과 DB Unique 제약 조건(uuid)을 이용해 멱등성을 보장하며 여행 일정 생성. 생성 실패 시(중복) 기존 항목 조회 후 반환.
- List<Trip> getTripList(Long userId) : 사용자의 여행 일정 목록 반환
# todo
- 없음
