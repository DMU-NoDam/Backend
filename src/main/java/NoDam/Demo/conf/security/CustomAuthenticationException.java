package NoDam.Demo.conf.security;

import NoDam.Demo.common.excetion.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * ExceptionTranslationFilter가 AuthenticationEntryPoint로 넘길 수 있도록
 * AuthenticationException을 상속하고 응답에 사용할 ErrorCode를 함께 전달한다
 */
@Getter
public class CustomAuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public CustomAuthenticationException(ErrorCode errorCode) {
        super(errorCode.message);
        this.errorCode = errorCode;
    }

}
