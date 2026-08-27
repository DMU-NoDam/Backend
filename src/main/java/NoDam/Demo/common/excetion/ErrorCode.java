package NoDam.Demo.common.excetion;

import org.springframework.http.HttpStatus;

/*
    code 규칙
    - 4자리 문자열, 첫 자리는 도메인 식별자, 나머지 3자리는 도메인 내 일련번호(001부터 증가)
    - 도메인 식별자
      0 : common (전 도메인 공용)
      1 : user / auth
      2 : site, region
      3 : place
      4 : trip
      5 : plan
      6 : 외부 연동 (flight, stay, weather, adapter)
 */
public enum ErrorCode {

    INVALID_REQUEST_PARAMETER("0001", HttpStatus.BAD_REQUEST, "잘못된 입력 파라미터입니다"),
    NOT_FOUND("0002", HttpStatus.NOT_FOUND, "not found"),
    CONFLICT("0003", HttpStatus.CONFLICT, "conflict"),
    BAD_REQUEST("0004", HttpStatus.BAD_REQUEST, "bad request"),
    NOT_AUTHOR("0005", HttpStatus.BAD_REQUEST, "작성자가 아닙니다"),
    ALREADY_PROCESSING("0006", HttpStatus.ACCEPTED, "already processing"),
    API_FAIL("0007", HttpStatus.CONFLICT, "api fail"),
    INTERNAL_SERVER_ERROR("0008", HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),

    EXPIRED_TOKEN("1001", HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),

    ALREADY_JOINED_TRIP("4001", HttpStatus.CONFLICT, "이미 참여한 여행입니다"),

    ;

    public String code;
    public HttpStatus status;
    public String message;

    ErrorCode(String code, HttpStatus status, String message){
        this.code = code;
        this.status = status;
        this.message = message;
    }

}
