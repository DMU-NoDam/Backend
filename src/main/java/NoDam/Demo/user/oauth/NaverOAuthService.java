package NoDam.Demo.user.oauth;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NaverOAuthService implements OAuthService {

    @Value("${oauth.naver.token-uri}")
    private String AccessTokenURI;
    @Value("${oauth.naver.user-info-uri}")
    private String UserInfoURI;
    @Value("${oauth.naver.redirect-uri}")
    private String RedirectURI;

    @Value("${oauth.naver.client-id}")
    private String ClientId;
    @Value("${oauth.naver.client-secret}")
    private String ClientSecret;

    @Override
    public String getAccessToken(String code) {
        return (String) WebClient.create(AccessTokenURI).post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", ClientId)
                        .queryParam("client_secret", ClientSecret)
                        .queryParam("code", code)
                        .queryParam("state", UUID.randomUUID().toString())
                        .build(true))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Error response: " + response.statusCode() + ", Body: " + body))))
                .bodyToMono(Map.class)
                .block()
                .get("access_token");
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        Map<String, String> userInfoMap = WebClient.create(UserInfoURI).post()
                .uri(uriBuilder -> uriBuilder
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Error response: " + response.statusCode() + ", Body: " + body))))
                .bodyToMono(Map.class)
                .map(response -> {
                    return (Map<String, String>) response.get("response");
                })
                .block();

        // String email = userInfoMap.get("email");
        return OAuthUserInfo.builder()
                .oAuthProvider("naver")
                .oAuthId(userInfoMap.get("id"))
                // .email(userInfoMap.get("email"))
                .name(userInfoMap.get("name"))
                // .number(userInfoMap.get("mobile"))
                // .nickName(userInfoMap.get("nickname"))
                .build();
    }

}
