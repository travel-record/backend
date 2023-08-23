package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@RequiredArgsConstructor
@Component
public class JwtParser {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private static final String AUTHORIZATION = "Authorization";
    private static final String USER_ID = "user_id";

    public String extractUserIdFrom(String token) {
        return (String) getClaimsFromToken(token).get(USER_ID);
    }

    public void verify(String token) {
        try {
            getClaimsFromToken(token);
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }
    }

    public Long extractUserIdFrom(HttpServletRequest request) {
        if (request == null || request.getHeader(AUTHORIZATION) == null) {
            return null;
        }

        String token = request.getHeader(AUTHORIZATION);
        verify(token);
        return Long.parseLong(extractUserIdFrom(token));
    }

    private Map<String, Object> getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
