package NoDam.Demo.conf.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthorityMapper {

    private static final String PREFIX = "ROLE_";

    public static SimpleGrantedAuthority toSpringAuthority(String role) {
        return new SimpleGrantedAuthority(PREFIX + role);
    }

}