package NoDam.Demo.common.excetion;

import NoDam.Demo.common.ErrorResponse;
import NoDam.Demo.conf.security.CustomAuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.errorCode.status)
                .body(ErrorResponse.of(e.errorCode));
    }

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(CustomAuthenticationException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.getErrorCode().status)
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_PARAMETER.status)
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST_PARAMETER));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_PARAMETER.status)
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST_PARAMETER));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

}
