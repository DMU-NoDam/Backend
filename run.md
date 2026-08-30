# Backend 실행 가이드

명령어만으로 빌드하고 `java -jar`로 실행합니다. 테스트는 건너뜁니다.

## 실행 방법

```bash
# 1) 빌드 (테스트 제외)
./gradlew clean build -x test

# 2) 실행

-- 처음 실행 (또는 db 값을 초기화할 때)
java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --spring.jpa.hibernate.ddl-auto=create

-- 두 번 째 실행
java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --spring.jpa.hibernate.ddl-auto=update
```

- 접속: `http://localhost:8080/back` (context-path `/back` 포함)
- Swagger: `http://localhost:8080/back/swagger-ui/index.html`
- 정상 기동 시 로그 마지막에 `Started DemoApplication ...` 출력

## 사전 준비물

| 항목 | 설명 |
|---|---|
| JDK | **21** 필수 (`java -version`으로 확인) |
| MySQL | **8.0**, `NoDam` 스키마(DB) 필요 |
| `.env` | 프로젝트 루트에 배치 (DB 접속 정보, API 키 등) |
| Gradle | 설치 불필요, `./gradlew` (wrapper) 사용 |

- `.env`는 `java -jar` 실행 위치(= 프로젝트 루트)에 있어야 자동으로 읽힙니다.
- 최소 필요 키: `DB_URL`(`host:port/스키마`), `DB_USERNAME`, `DB_PASSWORD`, `SECRET_KEY`, `AI_PROVIDER`(로컬은 `mock` 권장) 등.

---

## DB 초기화

DB를 완전히 비우고 처음 상태로 되돌리는 절차입니다. 프로젝트 루트에서 순서대로 실행합니다.
(`<user>`는 `.env`의 `DB_USERNAME` 값)

```bash
# 1) 스키마 삭제 후 재생성 (되돌릴 수 없음, .env의 DB_URL이 로컬인지 먼저 확인)
mysql -u <user> -p -e "DROP DATABASE IF EXISTS NoDam; CREATE DATABASE NoDam DEFAULT CHARACTER SET utf8mb4;"

# 2) 빌드
./gradlew clean build -x test

# 3) 테이블 생성 (초기 데이터가 없어 기동이 중단되고 종료됨 — 정상)
java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --spring.jpa.hibernate.ddl-auto=create

# 4) 초기 데이터 적재
mysql -u <user> -p < insert_places.sql

# 5) 정상 기동
java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --spring.jpa.hibernate.ddl-auto=update
```

- 3)에서 테이블은 만들어지지만 `InitDataChecker`가 초기 데이터 누락을 잡아 앱을 종료시킵니다. 그대로 4)로 넘어가면 됩니다. (→ 발생 가능한 에러 ⑨)
- `insert_places.sql`이 넣는 값: `region` 13건(일본 12 + 대한민국 id `9999`), 공항 `place` 10건(한국 id `1`~`6`, 일본 id `10`~`13`), mock `place` 7건, 공항 영업시간(`place-open`).
- 공항 `place`의 **id는 고정값**입니다. `AirportCode` enum이 이 id를 직접 참조하므로 바꾸면 항공 기능이 깨집니다.
- 초기화 결과 확인:
  ```bash
  mysql -u <user> -p -e "USE NoDam; SELECT COUNT(*) FROM region; SELECT COUNT(*) FROM place; SELECT COUNT(*) FROM place WHERE place_type = 'AIRPORT';"
  ```
  기대값 — `region` 13, `place` 17, `AIRPORT` 10
- `sql/backfill_trip_member_owner.sql`은 **기존 데이터 보정용**이라 새로 초기화할 때는 실행할 필요가 없습니다.
- 2회차부터는 위 5)만 실행하면 됩니다.

---

## 발생 가능한 에러

### ① JDK 버전 문제
```
error: invalid source release: 21
```
- 설치된 JDK가 21 미만. JDK 21 설치 후 `java -version` 확인.

### ② DB 연결 실패
```
Communications link failure
```
또는 `Access denied for user ...`.
- MySQL 서버 미실행 → `brew services start mysql` 등으로 실행.
- `.env`의 `DB_URL / DB_USERNAME / DB_PASSWORD`가 실제 DB와 일치하는지 확인.

### ③ DB 스키마 없음
```
Unknown database 'NoDam'
```
- `ddl-auto: update`는 테이블만 자동 생성, **데이터베이스는 만들지 않음**. 먼저 생성:
  ```bash
  mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS NoDam DEFAULT CHARACTER SET utf8mb4;"
  ```
- 스키마명은 `.env`의 `DB_URL` 끝부분과 일치해야 함.

### ④ 포트 8080 이미 사용 중
```
Port 8080 was already in use.
```
- 사용 중인 프로세스 종료:
  ```bash
  lsof -i :8080
  kill -9 <PID>
  ```
- 또는 다른 포트로 실행:
  ```bash
  java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --server.port=8081
  ```

### ⑤ jar 파일명이 다르거나 못 찾음
```
Error: Unable to access jarfile build/libs/Demo-0.0.1-SNAPSHOT.jar
```
- `settings.gradle`의 프로젝트명이나 `build.gradle`의 version이 바뀌면 jar 이름도 달라집니다. 실제 파일명 확인 후 그 이름으로 실행:
  ```bash
  ls build/libs/
  ```
- `-plain.jar`(있다면)는 실행용이 아님. `-plain`이 **없는** jar를 실행.

### ⑥ `.env` 누락 / 환경변수 비어 있음
```
Could not resolve placeholder 'DB_URL' in value "jdbc:mysql://${DB_URL}"
```
- `.env`가 없거나 필요한 키가 비어 있음. 루트에 배치하고 값 확인.
- **jar 실행 위치**에 `.env`가 있어야 읽힘.

### ⑦ 테스트 단계에서 멈춤
- 빌드 시 반드시 `-x test`로 테스트를 건너뜀:
  ```bash
  ./gradlew clean build -x test
  ```

### ⑧ `gradlew: Permission denied`
```bash
chmod +x ./gradlew
```

### ⑨ 초기 데이터 누락으로 기동 중단
```
ERROR ... 초기 데이터 누락 : region 테이블이 비어 있음
ERROR ... 초기 데이터 누락 : place 테이블이 비어 있음
ERROR ... 초기 데이터 누락 : place 테이블에 AIRPORT 장소가 없음
ERROR ... insert_places.sql 를 DB에 실행한 뒤 다시 기동하세요.
java.lang.IllegalStateException: 초기 데이터가 적재되지 않아 기동을 중단합니다. ...
```
- `conf` 패키지의 `InitDataChecker`가 기동 시 1회 `region` 건수 / `place` 건수 / `place`의 `AIRPORT` 건수를 검사하고, 하나라도 `0`이면 앱을 종료시킵니다.
- 해결: `mysql -u <user> -p < insert_places.sql` 실행 후 재기동. (→ DB 초기화 4~5 단계)
- 테이블 생성(`ddl-auto`)은 이 검사보다 먼저 끝나므로, **빈 DB로 띄워도 테이블은 만들어진 뒤** 중단됩니다. DB 초기화 3) 단계에서는 이 에러가 나는 것이 정상입니다.
- 검사를 끄려면 `application.yml`의 `init-data-check`를 `false`로 두거나, 실행 인자로 덮어씁니다:
  ```bash
  java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --init-data-check=false
  ```
  `init-data-check: true`일 때만 검사합니다.

### ⑩ `insert_places.sql` 실행 실패
```
ERROR 1054 (42S22): Unknown column 'status' in 'field list'
ERROR 1146 (42S02): Table 'NoDam.place-open' doesn't exist
```
- `insert_places.sql`이 현재 `Place` 엔티티에 **없는 컬럼/테이블**을 사용해서 나는 오류입니다.
  - 없는 컬럼: `status`, `name_en`, `name_jp`, `time`, `score`, `score_top3`, `summary`
  - 없는 테이블: `place-open`
- 엔티티(`place/domain/Place.java`)에 필드를 추가하거나, SQL을 현재 엔티티에 맞게 수정해야 합니다.
- 초기 데이터가 안 들어간 상태로 기동하면 ⑨ 에러로 이어집니다.
