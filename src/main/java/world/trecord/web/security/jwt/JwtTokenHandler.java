package world.trecord.web.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JwtTokenHandler {

    private static final String USER_ID = "user_id";

    public void verify(String secretKey, String token) {
        getClaimsFromToken(secretKey, token);
    }

    public String generateToken(Long userId, String secretKey, long expiredTimeMs) {
        Claims claims = Jwts.claims();
        claims.put(USER_ID, userId);

        Date issuedAt = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + expiredTimeMs))
                .signWith(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }

    public Long extractUserId(String secretKey, String token) {
        return getClaimsFromToken(secretKey, token).get(USER_ID, Long.class);
    }

    private Claims getClaimsFromToken(String secretKey, String token) {
        return Jwts.parserBuilder()
                .setSigningKey(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
