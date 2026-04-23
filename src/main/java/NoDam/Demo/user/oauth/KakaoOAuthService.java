package NoDam.Demo.user.oauth;

import io.netty.handler.codec.http.HttpHeaderValues;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    @Value("${oauth.kakao.token-uri}")
    private String AccessTokenURI;
    @Value("${oauth.kakao.user-info-uri}")
    private String UserInfoURI;
    @Value("${oauth.kakao.redirect-uri}")
    private String RedirectURI;

    @Value("${oauth.kakao.client-id}")
    private String ClientId;
    @Value("${oauth.kakao.client-secret}")
    private String ClientSecret;

    @Override
    public String getAccessToken(String code) {
        Map<String, String> kakaoToken = WebClient.create(AccessTokenURI).post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", ClientId)
                        .queryParam("code", code)
                        .queryParam("client_secret", ClientSecret)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE,
                        HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Error response: " + response.statusCode() + ", Body: " + body))))
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        return kakaoToken.get("access_token");
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        Map<String, Object> userInfoMap = WebClient.create(UserInfoURI)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(Map.class)
                .block();

        // String email = (String) ((Map<String, Object>) userInfoMap.get("kakao_account")).get("email");
        return OAuthUserInfo
                .builder()
                .oAuthProvider("kakao")
                .oAuthId( String.valueOf(userInfoMap.get("id")))
                // .nickName( (String) ((Map<String, Object>)userInfoMap.get("properties")).get("nickname") )
                .name((String) ((Map<String, Object>)userInfoMap.get("kakao_account")).get("name"))
                // .number((String) ((Map<String, Object>)userInfoMap.get("kakao_account")).get("phone"))
                // .email( (String) ((Map<String, Object>)userInfoMap.get("kakao_account")).get("email") )
                .build();
    }
}
