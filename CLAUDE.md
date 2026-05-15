# Backend Claude Context

## 프로젝트 개요
여행 일정 생성/관리 앱

## 의존성
- Java JDK 21
- SpringBoot 3.5.7
- MySQL 8.0
- JPA
- Sprint Security
- Lombok
- QueryDSL
- Swagger

- JUnit (test)
- Mockito (test)

## 프로젝트 설명

### facade layered Architecture

domain service, facade service 분리
다른 module간의 간섭이 없는 domain제외 controller에서 일반 service 호출 금지, facade service에서 repository class 직접 참조 금지

### Auth
jwt 사용
spring security에서 AuthenticationPrincipal로 사용자 정보 가져오기 (User entity 직접 주입됨)

### Common

BaseEntity 사용 
SuccessResponse 사용 (제네릭을 모두 명시한다)

### ErrorHandler, ErrorCode
spring error code와 custom Exception, error handler 사용
정의된 ErrorCode만 사용하며, 꼭 필요한 경우 사용자에게 알린다

### Aop Log
Controller, Service, Transaction 3군대에 Aop를 적용하여 Log 처리
Service에서 외부 api 호출 시 Log 찍기

## 팀 컨벤션

### Class.md 파일 관리
class.java 파일 옆에 {className}_class.md를 작성한다
(단, test 코드는 작성하지 않는다)

# Class 책임
- {class 책임 요소, 간단히 작성}
# 함수
- {public 함수 시그니처}
- - {추가 설명, 사용자 요청시에만 작성}
# todo
- {todo}
- - {간단 설명}

### Transaction
최대한 짧게 호출
오래 걸리는 작업, 외부 api 호출은 Transaction내부 호출 금지
평상시 transactional 어노테이션 사용, 필요시 Spring TransactionTemplate 사용 권장

### 동시성
select and write 의 동시성 발생 가능성을 확인하고
발생 가능시 service 함수위에 주석으로 알림

### uri 방식
/{module}/{접근 권한자}/uri ...

module : module 정리 참고
접근 권한자 : public, api, admin

### common package를 사용한다

- SuccessResponse 사용
Controller에서 반환할 때 SuccessResponse를 통해 반환하며, 제네릭 타입을 모두 명시한다

- util
필요한 경우 사용한다

## module 정리

### Common

공통 type
- schedule_type : LOOSE, NORMAL, TIGHT
- season : SPRING, SUMMER, FALL, WINTER
- weather : SUNNY, RAINY, SNOWY
- trip_theme : FOOD, HEALING, LANDMARK, ACTIVITY

exception
- CustomException
- ErrorCode

SuccessResponse

### User
책임 기능
- OAuth Register / Login
- Auth

### Site
지역 정보, 현재 일본만 있음

책임 기능
- 지역 날씨 정보 
- 지역 <-> 한국 환율 정보
- 지역 교통 수단 정보 (교통 수단 별 평균 금액)

### Place
장소 정보 저장

책임 기능
- place
- 좌표
- 추천

### Trip / Plan
여행 정보 / 일정 정보들

Plan 
 - place_plan : 장소 방문 plan
 - transport_plan : 이동 plan

책임 기능
- 여행 정보 저장
- 일정 정보

## 필수 지시

### git 사용 금지 
git 은 오직 사용자가 관리함
git diff, git status 모두 사용 금지

### .gradle-home 생성 금지
gradle은 사용자가 집접 실행함
compile error 발생해도 문제 없음

### CLAUD.md 수정 금지
CLAUD.md 파일 수정금지, 수정사항이 있다면 사용자에게 알려줄 것!
사용자가 직접 claud.md파일 수정을 요청할 때만 수정 가능

### 정확한 요구가 있는 경우 바로 수정
사용자의 정확한 요구가 있는 경우 다른 코드들을 확인하지 않고 바로 요구사항을 수행한다

### 사용자가 작성한 주석을 지우지 말 것
수정하는 부분이 아닌 부분의 주석들을 지우지 말 것

# CLAUDE.md

## 1. Think Before Coding

Don't assume. Don't hide confusion. Surface tradeoffs.

Before implementing:

State your assumptions explicitly. If uncertain, ask.
If multiple interpretations exist, present them - don't pick silently.
If a simpler approach exists, say so. Push back when warranted.
If something is unclear, stop. Name what's confusing. Ask.
