package NoDam.Demo.user.dto.response;

import NoDam.Demo.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserInfoDto {

    private String email;
    private String name;

    private boolean isOAuthUser;
    private String provider;

    public static UserInfoDto of(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.email = user.getEmail();
        dto.name = user.getName();
        dto.isOAuthUser = user.isOAuthUser();
        dto.provider = user.getOAuthProvider();
        return dto;
    }

}
