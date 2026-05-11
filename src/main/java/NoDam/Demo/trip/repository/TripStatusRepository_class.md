# Class 책임
- Trip isPlanning 상태 낙관적 업데이트 (플래닝 진행 중 여부 관리)
# 함수
- int tryUpdateTripStatus(Long id, Boolean oldStatus, Boolean updateStatus)
- - CAS 방식으로 상태 전환, 영향받은 row 수 반환 (1이면 성공, 0이면 실패)
- - transaction 필수
# todo
