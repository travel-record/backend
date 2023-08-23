package world.trecord.web.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JwtGenerator {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private static final String USER_ID = "user_id";
    private static final long EXPIRATION_TIME = 86400000L;
    private static final long REFRESH_TOKEN_EXPIRATION_MULTIPLIER = 14;

    public String generateToken(Long userId) {
        return generateToken(userId, EXPIRATION_TIME);
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, EXPIRATION_TIME * REFRESH_TOKEN_EXPIRATION_MULTIPLIER);
    }

    private String generateToken(Long userId, long expirationTime) {
        Claims claims = Jwts.claims();
        claims.put(USER_ID, String.valueOf(userId));
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }
}
