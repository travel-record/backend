package world.trecord.web.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JwtTokenHandler {

    private static final String USER_ID = "user_id";

    public String generateToken(Long userId, String secretKey, long expiredTimeMs) {
        Claims claims = Jwts.claims();
        claims.put(USER_ID, String.valueOf(userId));

        Date issuedAt = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + expiredTimeMs))
                .signWith(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }

    public String extractUserId(String secretKey, String token) {
        return (String) getClaimsFromToken(secretKey, token).get(USER_ID);
    }

    public void verify(String secretKey, String token) {
        try {
            getClaimsFromToken(secretKey, token);
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }
    }

    private Map<String, Object> getClaimsFromToken(String secretKey, String token) {
        return Jwts.parserBuilder()
                .setSigningKey(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
