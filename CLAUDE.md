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

domain service, facade service 분리 (단, 다른 domain간의 간섭이 없거나 매우 간단한 domain제외)
- 정의
domain에 여러 entity를 가질 수 있으며, 같은 domain에 소속된 entity는 로직에서 서로 섞여도 무관하다
domain service : Repository를 통해 domain의 로직을 처리한다
facade service : 여러 domain이 관련된 기능들을 domain service들을 사용하여 처리한다
repositroy를 가지지 않아도 domain 규칙을 가지면 domain service 분리 가능

ai service와 같이 외부 service를 위한 class는 repository class와 동일하게 취급한다(output port)
- 목적
domain service는 다른 domain에 섞이지 않게 하기 위함
facade service는 가급적 흐름만을 책임지기 위함
- 규칙
controller에서 domain service 호출 금지
facade service에서 repository class 직접 참조 금지
domain service에서 다른 domain의 repository 호출 금지

단, 모든 규칙은 새로 코드를 작성할 때만 적용한다 (이미 작성된 코드가 기준을 만족하지 못해도 사용자가 요청하기 전까지 수정하지 않는다) 

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

### Transaction
최대한 짧게 호출
오래 걸리는 작업, 외부 api 호출은 Transaction내부 호출 금지
평상시 transactional 어노테이션 사용, 필요시 Spring TransactionTemplate 사용 권장
class에 transactional 어노테이션 사용을 금지한다

### 동시성
select and write 의 동시성 발생 가능성을 확인하고
발생 가능시 service 함수위에 주석으로 알림

### uri 방식
/{model}/{접근 권한자}/uri ...
접근 권한자 : public, api, admin

### common package를 사용한다

- SuccessResponse 사용
Controller에서 반환할 때 SuccessResponse를 통해 반환하며, 제네릭 타입을 모두 명시한다

- util
필요한 경우 사용한다

## model 정리

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

### Trip
여행 정보

entity
- Trip
- UserFixedTrip (fix 여부 저자 table)

### Plan
일정 정보들

entity
- Plan
- DatePlan
- - 하루의 plan 정보를 담고 있음
- PlacePlan
- - 장소 일정을 저장함
- TransportPlan
- - 이동 일정을 저장함

책임 기능
- 일정 정보

### Region
지역 정보를 담당함

## 필수 지시

### git 사용 금지 
git 은 오직 사용자가 관리함
git diff, git status 모두 사용 금지

### .gradle-home 생성 금지
gradle은 사용자가 집접 실행함
compile error 발생해도 문제 없음

### CLAUDE.md 수정 금지
CLAUD.md 파일 수정금지, 수정사항이 있다면 사용자에게 알려줄 것!
사용자가 직접 claude.md파일 수정을 요청할 때만 수정 가능

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

### 2. 존댓말 사용
존댓말 사용할 것
장난치지 말 것
