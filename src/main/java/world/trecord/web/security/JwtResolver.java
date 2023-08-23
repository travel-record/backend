package world.trecord.web.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@RequiredArgsConstructor
@Component
public class JwtResolver {

    @Value("${jwt.secret-key}")
    private String secretKey;

    public String extractUserIdFrom(String token) {
        Object userId = Jwts.parserBuilder()
                .setSigningKey(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("user_id");

        if (userId instanceof Integer) {
            return String.valueOf(userId);
        }

        return (String) Jwts.parserBuilder()
                .setSigningKey(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("user_id");
    }

    public void verify(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }
    }

    public Long extractUserIdFrom(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null) {
            return null;
        }

        verify(token);
        return Long.parseLong(extractUserIdFrom(token));
    }

}
