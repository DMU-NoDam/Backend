package NoDam.Demo.conf.security;

import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.repository.UserRepository;
import NoDam.Demo.user.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger("AccessTokenFilter :: ");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String bearer = request.getHeader("Authorization");
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = bearer.substring(7);

        Long userId = jwtService.decodeAccessToken(token);
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userOpt.get(), null));
            log.info("Access Token Success userId = " + userId);
        } else {
            log.info("Access Token Fail");
        }

        filterChain.doFilter(request, response);
    }
}
