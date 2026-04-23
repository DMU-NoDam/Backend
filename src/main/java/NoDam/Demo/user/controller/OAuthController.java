package NoDam.Demo.user.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.dto.response.UserInfoDto;
import NoDam.Demo.user.oauth.OAuthService;
import NoDam.Demo.user.oauth.OAuthUserInfo;
import NoDam.Demo.user.service.JWTService;
import NoDam.Demo.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/public/oauth")
@Tag(name = "OAuthController")
public class OAuthController {

    private final UserService userService;
    private final JWTService jwtService;

    private final OAuthService googleOAuthService;
    private final OAuthService kakaoOAuthService;
    private final OAuthService naverOAuthService;

    @Value("${oauth.google.client-id}")
    private String googleClientId;
    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth.naver.client-id}")
    private String naverClientId;
    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;

    @GetMapping
    public void redirectToOAuthProvider(
            @RequestParam("provider") String provider,
            HttpServletResponse response
    ) throws IOException {
        String url = "";
        switch (provider) {
            case "google":
                url = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId +
                        "&redirect_uri=" + googleRedirectUri +
                        "&response_type=code&scope=profile";
                break;
            case "kakao":
                url = "https://kauth.kakao.com/oauth/authorize?client_id=" + kakaoClientId +
                        "&redirect_uri=" + kakaoRedirectUri +
                        "&response_type=code";
                break;
            case "naver":
                url = "https://nid.naver.com/oauth2.0/authorize?client_id=" + naverClientId +
                        "&redirect_uri=" + naverRedirectUri +
                        "&response_type=code";
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        response.sendRedirect(url);
    }

    @GetMapping("/{provider}")
    public ResponseEntity loginWithOAuth(
            @RequestParam("code") String code,
            @PathVariable("provider") String provider
    ) {
        if(code == null || code.isEmpty())
            throw new CustomException(ErrorCode.INVALID_REQUEST_PARAMETER);

        OAuthService oAuthService = null;
        switch(provider) {
            case "google":
                oAuthService = googleOAuthService; break;
            case "naver":
                oAuthService = naverOAuthService; break;
            case "kakao":
                oAuthService = kakaoOAuthService; break;
            default:
                throw new CustomException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }

        OAuthUserInfo userInfo = oAuthService.getUserInfo(oAuthService.getAccessToken(code));
        User user = userService.loginWithOAuth(userInfo);

        String accessToken = jwtService.generateAccessToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse(
                "success",
                Map.of(
                        "user", UserInfoDto.of(user),
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                )
        ));
    }
}
