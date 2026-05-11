# Class 책임
- Trip 엔티티에 대한 DB 접근
# 함수
- Optional<Trip> findByUuid(String uuid)
- List<Trip> findAllByUserId(Long userId)

- boolean existsOverlapFixedTrip(Long userId, Long tripId, LocalDate startDate, LocalDate endDate) 
특정 기간 내에 해당 사용자의 다른 고정된 일정이 존재하는지 확인

- Optional<Trip> findFixedTripByDate(Long userId, LocalDate date)
특정 사용자의 특정 날짜에 고정된 여행 일정 조회

# todo
- 없음
