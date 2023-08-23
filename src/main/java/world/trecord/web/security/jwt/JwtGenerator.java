package world.trecord.web.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JwtGenerator {

    private static final String USER_ID = "user_id";

    public String generateToken(Long userId, String secretKey, long expiredTimeMs) {
        Claims claims = Jwts.claims();
        claims.put(USER_ID, String.valueOf(userId));
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiredTimeMs))
                .signWith(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }
}
