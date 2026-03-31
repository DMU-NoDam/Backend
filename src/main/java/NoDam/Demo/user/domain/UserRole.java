package NoDam.Demo.user.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public enum UserRole {

    USER(), // user /*/visite/** 접근 불가
    VISITOR(UserRole.USER), // todo : delete UserRole.USER!! (개발 환경에서 세팅)
    ADMIN(UserRole.USER),
    ;

    private List<String> roles;

    UserRole(UserRole... plusRoles) {
        this.roles = new ArrayList<>(Arrays.stream(plusRoles).map(Enum::name).toList());
        roles.add(this.name());
    }

    // ex) UserRole.getAuthorities(AuthorityMapper::toSpringAuthority)
    public <R> Collection<R> getAuthorities(Function<String, R> convertFunction) {
        return roles.stream()
                .map(role -> convertFunction.apply(role))
                .toList();
    }

}
