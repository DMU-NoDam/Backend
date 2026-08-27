package NoDam.Demo.user.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.jwt.JWTException;
import NoDam.Demo.user.jwt.JWTUtil;
import NoDam.Demo.user.jwt.JWTUtil.TokenDto;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JWTService {

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

    public Long decodeAccessToken(String token) throws JWTException {
        TokenDto tokenDto = decodeToken(token);
        return Long.valueOf(tokenDto.getSubject().toString());
    }

    public Long decodeRefreshToken(String token) throws JWTException {
        TokenDto tokenDto = decodeToken(token);
        return Long.valueOf(tokenDto.getSubject().toString());
    }

    private TokenDto decodeToken(String token) throws JWTException {
        try {
            return JWTUtil.decodeToken(token, secretKey);
        } catch (JWTException e) {
            if (e.getCause() instanceof ExpiredJwtException) {
                throw new CustomException(ErrorCode.EXPIRED_TOKEN);
            }
            throw e;
        }
    }

}
