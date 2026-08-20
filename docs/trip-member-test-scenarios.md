# Trip 권한/멤버/초대 기능 테스트 시나리오

이번에 추가된 기능(Role, 멤버 관리, 초대 링크, OWNER 위임 나가기, 여행 삭제)을 로컬 환경에서
Swagger UI로 직접 확인하기 위한 시나리오입니다. 실제 OAuth 로그인 없이 `TestController`의 테스트 유저
발급 API로 여러 계정을 만들어 검증합니다.

> 지정 초대(userId로 콕 집어 초대 + 수락) 기능은 제거되었습니다. 이제 초대는 링크 방식 하나만 있으며,
> 로그인한 사용자는 누구든 링크(token)로 수락 절차 없이 바로 참여합니다.

## 0. 준비

이미 이전에 앱을 한 번 이상 기동해서 `trip_invitation` 테이블이 만들어져 있다면(즉 `status`, `invitee_user_id`
컬럼이 있는 옛날 스키마), 지정 초대 기능 제거로 엔티티에서 그 두 컬럼이 빠졌습니다. `ddl-auto=update`는 컬럼을
자동으로 지워주지 않고, 특히 `status`는 `NOT NULL`이라 그대로 두면 이후 링크 발급(insert) 시
`Field 'status' doesn't have a default value` 에러가 날 수 있습니다. 아래 SQL을 먼저 1회 실행해 주세요.

```sql
ALTER TABLE trip_invitation DROP COLUMN status, DROP COLUMN invitee_user_id;
```

그 다음 빌드/실행합니다.

```bash
./gradlew clean build -x test
java -jar build/libs/Demo-0.0.1-SNAPSHOT.jar --spring.jpa.hibernate.ddl-auto=update
```

- Swagger: `http://localhost:8080/back/swagger-ui/index.html`
- 신규 Trip부터 테스트하는 것이 편합니다(기존 Trip 데이터가 있다면 `sql/backfill_trip_member_owner.sql`을 먼저
  1회 실행해서 `trip_member` 테이블을 채워야 기존 Trip들도 조회/삭제가 정상 동작합니다).

## 1. 테스트 유저 발급

`POST /test/user` (인증 불필요, body에 `id: null`이면 신규 생성)

```json
{ "role": "USER", "id": null }
```

두 번 이상 호출해서 `userA`, `userB`의 `userId` / `accessToken`을 각각 확보합니다.
Swagger 우측 상단 **Authorize** 버튼에는 `Bearer` 없이 **accessToken 값만** 넣습니다.

## 2. 여행 생성 + OWNER 자동 등록 확인

- (userA로 인증) `POST /trip/api` 로 여행 생성 → 응답의 `id`가 tripId
- `GET /trip/api/{tripId}/members` 호출 → userA가 `role: OWNER`로 이미 등록되어 있는지 확인
- `GET /trip/api` 로 userA의 여행 목록에 방금 만든 여행이 보이는지 확인

## 3. 초대 링크로 참여

- Authorize를 userA(OWNER)로 유지한 채 `POST /trip/api/{tripId}/invitations/link` 호출 → `token` 획득
- 같은 tripId로 한 번 더 호출 → 같은 token이 반환되는지 확인(여행당 하나 재사용)
- Authorize 해제(또는 새 브라우저 시크릿 모드) 후 `GET /trip/public/invitations/{token}` 호출 → 로그인 없이 여행 이름/기간만 보이는지 확인
- Authorize를 userB로 교체 → `POST /trip/api/invitations/{token}/join` (수락 절차 없이 바로 참여)
- `GET /trip/api/{tripId}/members` → userB가 `MEMBER`로 추가됐는지 확인
- userB가 같은 token으로 한 번 더 `join` 호출 → 에러 없이 성공(멱등) 확인
- userB로 `GET /trip/api/{tripId}` 호출 → 여행 상세를 정상 조회할 수 있는지 확인 (MEMBER도 일정 공유/조회 가능)

## 4. 나가기 (MEMBER)

- Authorize를 userB로 교체 → `POST /trip/api/{tripId}/leave` (body 없이)
- `GET /trip/api/{tripId}/members` → userB가 사라졌는지 확인

## 5. OWNER 위임 나가기

3번 링크로 userB를 다시 참여시켜 멤버 구성을 userA(OWNER), userB(MEMBER)로 만든 뒤:

- Authorize를 userA로 교체
- `POST /trip/api/{tripId}/leave` body `{ "newOwnerUserId": {userB의 id} }`
- `GET /trip/api/{tripId}/members` → userA는 사라지고 userB가 `OWNER`로 바뀌었는지 확인
- (실패 케이스) userA가 다시 같은 여행에서 `newOwnerUserId` 없이 leave를 시도하면 애초에 멤버가 아니므로 `NOT_AUTHOR` 확인

## 6. 혼자 남은 OWNER 나가기 = 삭제

현재 멤버 구성: userB(OWNER) 혼자

- Authorize를 userB로 교체 → `POST /trip/api/{tripId}/leave` (body 없이)
- 내부적으로 `deleteTrip`이 호출됨 → `GET /trip/api` (userB) 목록에서 해당 여행이 사라졌는지 확인
- `GET /trip/api/{tripId}` 재조회 시 `NOT_FOUND` 확인

## 7. 여행 삭제 (OWNER 전용, 멤버 있는 상태)

새 여행을 하나 더 만들어(userA로 생성) 초대 링크로 userB를 참여시킨 뒤:

- (userB, MEMBER) `DELETE /trip/api/{tripId}` 호출 → `NOT_AUTHOR` 확인 (OWNER만 가능)
- (userA, OWNER) `DELETE /trip/api/{tripId}` 호출 → 성공
- userA, userB 각각 `GET /trip/api`로 목록 확인 → 둘 다에게서 사라졌는지 확인 (soft delete + 멤버/초대 정리 검증)
