package NoDam.Demo.user.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.jwt.JWTException;
import NoDam.Demo.user.jwt.JWTUtil;
import NoDam.Demo.user.jwt.JWTUtil.TokenDto;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JWTService {

    public static String ACCESS = "ACCESS";

    @Value("${token.SECRET_KEY}")
    private String secretKey;

    @Value("${token.ACCESS_EXPIRATION_SECOND}")
    private Long AccessExpireSecond;

    @Value("${token.REFRESH_EXPIRATION_SECOND}")
    private Long RefreshExpireSecond;

    public String generateAccessToken(Long userId) {
        return JWTUtil.encodeToken(new TokenDto(userId, Map.of()), AccessExpireSecond, secretKey);
    }

    public String generateRefreshToken(Long userId) {
        return JWTUtil.encodeToken(new TokenDto(userId, Map.of()), RefreshExpireSecond, secretKey);
    }

    public Long decodeAccessToken(String token) {
        TokenDto tokenDto;
        try {
            tokenDto = JWTUtil.decodeToken(token, secretKey);
        } catch (JWTException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.CONFLICT);
        }
        return Long.valueOf(tokenDto.getSubject().toString());
    }

    public Long decodeRefreshToken(String token) {
        TokenDto tokenDto;
        try {
            tokenDto = JWTUtil.decodeToken(token, secretKey);
        } catch (JWTException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.CONFLICT);
        }
        return Long.valueOf(tokenDto.getSubject().toString());
    }

}
