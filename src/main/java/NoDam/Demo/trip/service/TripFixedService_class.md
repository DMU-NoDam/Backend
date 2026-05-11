# Class 책임
- 여행 고정(Fixed) 관련 비즈니스 로직 처리
- UserFixedTrip을 활용한 일 단위 일정 중복 방지 및 동시성 제어
# 함수
- public Optional<Trip> getTodayTrip(Long userId)
- - 사용자의 오늘 날짜에 고정된 여행 일정을 조회한다
- public Trip updateTripFixed(Long userId, Trip trip, boolean isFixed)
- - 여행의 고정 여부를 업데이트한다
- - 고정(True) 요청 시 비관적 락을 통해 기간 중복을 체크하고 UserFixedTrip에 데이터를 생성한다
- - 해제(False) 요청 시 UserFixedTrip에서 해당 데이터를 삭제한다
# todo
