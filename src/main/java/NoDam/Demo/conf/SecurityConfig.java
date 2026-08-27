package NoDam.Demo.conf;

import NoDam.Demo.conf.security.AccessTokenFilter;
import NoDam.Demo.conf.security.CustomAuthenticationException;
import NoDam.Demo.conf.security.CustomAuthorizationManager;
import NoDam.Demo.user.domain.UserRole;
import NoDam.Demo.user.repository.UserRepository;
import NoDam.Demo.user.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JWTService jwtService;

    // 직접 설정한 예외가 아닌 경우 사용할 Spring Security 기본 EntryPoint
    private final AuthenticationEntryPoint defaultAuthenticationEntryPoint = new Http403ForbiddenEntryPoint();

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/*/public/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/*/api/**").access(new CustomAuthorizationManager(AuthorityAuthorizationManager.hasRole(UserRole.USER.name())))
                        .requestMatchers("/*/admin/**").access(new CustomAuthorizationManager(AuthorityAuthorizationManager.hasRole(UserRole.ADMIN.name())))
                        .anyRequest().permitAll()
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authenticationException) -> {
                            if (authenticationException instanceof CustomAuthenticationException) {
                                handlerExceptionResolver.resolveException(request, response, null, authenticationException);
                                return;
                            }
                            defaultAuthenticationEntryPoint.commence(request, response, authenticationException);
                        })
                )

                .addFilterBefore(new AccessTokenFilter(jwtService, userRepository), LogoutFilter.class)
        ;

        return http.build();
    }

}
