package world.trecord.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JwtTokenHandler {

    private static final String USER_ID = "user_id";
    private static final String SUBJECT = "authentication";

    public void verifyToken(String secretKey, String token) {
        try {
            Claims claims = doVerifyTokenAndGetClaims(secretKey, token);
            if (!Objects.equals(claims.getSubject(), SUBJECT)) {
                throw new JwtException("Invalid subject in the token");
            }
        } catch (Exception e) {
            throw new JwtException(e.getMessage(), e);
        }
    }

    public String generateToken(Long userId, String secretKey, long expiredTimeMs) {
        Claims claims = Jwts.claims();
        claims.put(USER_ID, userId);

        Date issuedAt = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(SUBJECT)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + expiredTimeMs))
                .signWith(doGetSignKey(secretKey))
                .compact();
    }

    public Long getUserIdFromToken(String secretKey, String token) {
        try {
            Claims claims = doVerifyTokenAndGetClaims(secretKey, token);
            return claims.get(USER_ID, Long.class);
        } catch (Exception e) {
            throw new JwtException(e.getMessage(), e);
        }
    }

    private Claims doVerifyTokenAndGetClaims(String secretKey, String token) {
        return Jwts.parserBuilder()
                .setSigningKey(doGetSignKey(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey doGetSignKey(String secretKey) {
        return hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
