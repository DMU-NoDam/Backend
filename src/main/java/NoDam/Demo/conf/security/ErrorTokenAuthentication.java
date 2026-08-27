package NoDam.Demo.conf.security;

import NoDam.Demo.common.excetion.ErrorCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class ErrorTokenAuthentication extends AbstractAuthenticationToken {

    private final ErrorCode errorCode;

    public ErrorTokenAuthentication(ErrorCode errorCode) {
        super(null);
        this.errorCode = errorCode;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

}
