package NoDam.Demo.user.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    @AllArgsConstructor
    @Getter
    public static class TokenDto {
        private Object subject;
        private Map<String, Object> claims;
    }

    public static String encodeToken(
            TokenDto requestTokenDto,
            Long expireSecond,
            String secretKey
    ) {
        return Jwts.builder()
                .setSubject(requestTokenDto.subject.toString())
                .addClaims(requestTokenDto.claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireSecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public static TokenDto decodeToken(
            String token,
            String secretKey
    ) throws JWTException {
        Claims claims;
        try {
            claims = Jwts
                    .parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException | UnsupportedJwtException e) {
            throw new JWTException(e);
        }

        return new TokenDto(claims.getSubject(), claims);
    }
}
