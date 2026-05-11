# Class 책임
- 이동 일정 엔티티
# 함수
- public TransportPlan(Trip trip, LocalDateTime startTime, LocalDateTime endTime, Integer takeTime, Long toPlaceId, Long fromPlaceId, String googleId) : 빌더를 통한 생성
# todo
- google 정보 저장
- - google 길찾기 이후 정보 값을 같이 저장할 필요가 있음 (현제는 단순 googleId로만 처리함)