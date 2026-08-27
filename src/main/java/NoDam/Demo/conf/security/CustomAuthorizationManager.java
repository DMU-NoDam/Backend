package NoDam.Demo.conf.security;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/*
    token filter -> exception translation filter -> authentic filter -> request dispatcher (with exception handler)

    expired token : token filter : try resolve token -> jwt util : throw expired exception -> token filter : set ErrorTokenAuthentic
    -> authentic filter : check authentic (with CustomAuthorizationManager), throw CustomAuthenticationException
    -> exception translation filter : resolver.resolve(CustomAuthenticationException)
    -> HandlerExceptionResolver.reslove : write response
 */
@RequiredArgsConstructor
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final AuthorizationManager<RequestAuthorizationContext> delegate;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {

        if (authentication.get() instanceof ErrorTokenAuthentication errorTokenAuthentication) {
            throw new CustomAuthenticationException(errorTokenAuthentication.getErrorCode());
        }

        return delegate.check(authentication, context);
    }

}
