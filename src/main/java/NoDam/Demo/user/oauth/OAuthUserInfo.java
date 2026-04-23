package NoDam.Demo.user.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class OAuthUserInfo {

    private String oAuthProvider;
    private String oAuthId;

    private String name;
    private int age;

}
