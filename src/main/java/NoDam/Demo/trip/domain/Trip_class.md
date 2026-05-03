# Class 책임
- 여행 일정 기본 정보 엔티티
# 함수
- public Trip(String name, Long userId, String uuid, Long siteId, int personCount, ScheduleType scheduleType, TransportType transportType, TripThemeType tripThemeType, LocalDate startDate, LocalDate endDate, Long totalPrice) : 빌더를 통한 생성
# todo
- Version 처리 필요
plan 의 정보가 update 될 때 마다 version을 올림 (동시성 문제, 변경 사항 확인용)
동시성 처리 : 낙관적 lock with versions
변경 사항 확인 : version 과 같이 request → back version 먼저 확인하고 version이 update 되었다면 json 반환

- SiteId
site 가 일본 외에 더 많이 생겨서 site 구현을 한다면
site_id not null 처리

- TripThemeType
plan들이 모두 생성되고 사용자가 plan을 고르면
해당 plan의 theme를 trip에 저장함
이후 계획 수정에 trip_theme가 필요함
  (여행 수정기능에서 activity theme 장소에 카페가 들어가지 않도록)