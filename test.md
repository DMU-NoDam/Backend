# cli test

port : 8080

## test 진행
curl을 통해 요청을 보내고 반환값을 저장한다

예시 curl
```
curl -X POST http://localhost:8080/test/user \
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

## 필수 규칙
db, env 를 읽지 말것
api가 실패했다면 test를 중단하고 사용자에게 알릴 것
