# Class 책임
- 여행 일정 기본 정보 엔티티
# 함수
- public Trip(...) : 빌더를 통한 생성
# todo
- Version 처리 필요
plan 의 정보가 update 될 때 마다 version을 올림 (동시성 문제, 변경 사항 확인용)
동시성 처리 : 낙관적 lock with versions
변경 사항 확인 : version 과 같이 request → back version 먼저 확인하고 version이 update 되었다면 json 반환