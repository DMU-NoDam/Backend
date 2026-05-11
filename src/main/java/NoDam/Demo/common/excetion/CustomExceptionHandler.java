package NoDam.Demo.common.excetion;

import NoDam.Demo.common.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<SuccessResponse<String>> handleCustomException(CustomException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.errorCode.status)
                .body(new SuccessResponse<>(e.errorCode.message, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SuccessResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_PARAMETER.status)
                .body(new SuccessResponse<>(ErrorCode.INVALID_REQUEST_PARAMETER.message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SuccessResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_PARAMETER.status)
                .body(new SuccessResponse<>(ErrorCode.INVALID_REQUEST_PARAMETER.message, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SuccessResponse<String>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(new SuccessResponse<>("Internal Server Error", e.getMessage()));
    }
}
