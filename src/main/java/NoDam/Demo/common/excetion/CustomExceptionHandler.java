package NoDam.Demo.common.excetion;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity handleCustomException(CustomException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.errorCode.status).body(e.errorCode.message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_PARAMETER.status).body(ErrorCode.INVALID_REQUEST_PARAMETER.message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_PARAMETER.status).body(ErrorCode.INVALID_REQUEST_PARAMETER.message);
    }
}
