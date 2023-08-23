package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@RequiredArgsConstructor
@Component
public class JwtParser {

    private static final String USER_ID = "user_id";

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
