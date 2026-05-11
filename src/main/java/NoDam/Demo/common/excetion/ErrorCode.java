package NoDam.Demo.common.excetion;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 입력 파라미터입니다"),

    NOT_FOUND(HttpStatus.NOT_FOUND, "not found"),
    NOT_AUTHOR(HttpStatus.BAD_REQUEST, "작성자가 아닙니다"),
    CONFLICT(HttpStatus.CONFLICT, "conflict"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad request"),

    ALREADY_PROCESSING(HttpStatus.ACCEPTED, "already processing"),

    ;

    public HttpStatus status;
    public String message;

    ErrorCode(HttpStatus status, String message){
        this.status = status;
        this.message = message;
    }

}
