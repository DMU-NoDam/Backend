package NoDam.Demo.user.oauth;

public interface OAuthService {

    String getAccessToken(String code);
    OAuthUserInfo getUserInfo(String accessToken);

}
