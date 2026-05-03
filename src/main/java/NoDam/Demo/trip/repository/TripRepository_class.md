# Class 책임
- Trip 엔티티에 대한 DB 접근
# 함수
- Optional<Trip> findByUuidWithLock(String uuid) : uuid로 비관적 락을 걸어 조회
- List<Trip> findAllByUserId(Long userId) : 특정 사용자의 모든 여행 일정 조회
# todo
- 없음
