package NoDam.Demo.user.oauth;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {

    @Value("${oauth.google.token-uri}")
    private String AccessTokenURI;
    @Value("${oauth.google.user-info-uri}")
    private String UserInfoURI;
    @Value("${oauth.google.redirect-uri}")
    private String RedirectURI;

    @Value("${oauth.google.client-id}")
    private String ClientId;
    @Value("${oauth.google.client-secret}")
    private String ClientSecret;

    @Override
    public String getAccessToken(String code) {
        Map<String, Object> googleTokenResponse = WebClient.create(AccessTokenURI).post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("client_id", ClientId)
                        .queryParam("client_secret", ClientSecret)
                        .queryParam("code", code)
                        .queryParam("redirect_uri", RedirectURI)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.CONTENT_LENGTH, "0")
                //.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Error response: " + response.statusCode() + ", Body: " + body))))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        return (String) googleTokenResponse.get("access_token");
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        Map<String, String> userInfoMap = WebClient.create(UserInfoURI).get()
                .uri(uriBuilder -> uriBuilder
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (response) -> Mono.error(new RuntimeException(response.toString())))
                .bodyToMono(Map.class)
                .block();

        // String email = userInfoMap.get("email");
        return OAuthUserInfo.builder()
                .oAuthProvider("google")
                .oAuthId(userInfoMap.get("id"))
                // .email(userInfoMap.get("email"))
                .name(userInfoMap.get("name"))
                // .nickName(userInfoMap.get("given_name"))
                .build();
    }
}
