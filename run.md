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

## 발생 가능한 에러

### ① JDK 버전 문제
```
error: invalid source release: 21
```
- 설치된 JDK가 21 미만. JDK 21 설치 후 `java -version` 확인.
- 여러 JDK 사용 시 (macOS):
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 21)
  ```

### ② DB 연결 실패
```
Communications link failure
```
또는 `Access denied for user ...`.
- MySQL 서버 미실행 → `brew services start mysql` 등으로 실행.
- `.env`의 `DB_URL / DB_USERNAME / DB_PASSWORD`가 실제 DB와 일치하는지 확인.
- 원격 DB(`nodam.duckdns.org:3306`)는 방화벽/네트워크로 막힐 수 있음 → 로컬 MySQL로 전환.

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
