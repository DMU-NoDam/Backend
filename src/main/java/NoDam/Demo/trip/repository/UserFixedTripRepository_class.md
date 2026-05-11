# Class 책임
- 사용자의 날짜별 고정 여행 일정(UserFixedTrip) JPA Repository
# 함수
- List<UserFixedTrip> findAllByUserIdAndDateRangeForUpdate(Long userId, LocalDate startDate, LocalDate endDate)
- - 특정 기간 동안 사용자의 고정 일정을 비관적 락(PESSIMISTIC_WRITE)을 걸어 조회한다
- List<UserFixedTrip> findAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate)
- - 특정 기간 동안 사용자의 고정 일정을 조회한다 (Lock 없음)
- void deleteByUserIdAndTrip(Long userId, Trip trip)
- - 특정 사용자의 특정 여행 고정 데이터를 삭제한다
# todo
