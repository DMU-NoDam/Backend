# cli test

port : 8080
기본 url : http://localhost:8080/back

## test 진행
curl을 통해 요청을 보내고 반환값을 저장한다

예시 curl
```
curl -X POST http://localhost:8080/back/test/user \
  -H "Content-Type: application/json" \
  -d '{"id": null, "role": "USER"}'
```

모든 api를 순서대로 호출하고 호출한 api payload와 response를 정리하여 출력한다

## 중요 api

### test create user api
escription**: 테스트용 사용자를 생성하거나 기존 사용자의 토큰을 발급받는 API입니다. 신규 생성 시 `id`를 `null`로, 기존 사용자 토큰 재발급 시 해당 `userId`를 입력합니다.
```
URI: `/test/user`
Method: `POST`
Body: json
{
  "id": null,
  "role": "USER"
}
```

## test 시나리오
 1. 유저 생성
 2. trip 생성
 3. plan 생성 상태 조회
 4. 전체 plan 조회
 4. theme 선택
 4. place 추천 (place plan에 다른 장소 추천)
 5. plan 수정 / 삭제
 6. trip fix
 7. trip 삭제
 
## test 필요 없는 api
 항공기, 숙소 api는 test skip할 것 (필요시 사용자 직접 요청함)
   
## 필수 규칙
 - db, env 를 읽지 말것
 - api가 실패했다면 test를 중단하고 사용자에게 알릴 것
 - gradle 관련 명령어 금지
