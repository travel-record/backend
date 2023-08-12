package world.trecord.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;
    public static final long EXPIRATION_TIME = 86400000L;

    public String createTokenWith(Long userId) {
        Claims claims = Jwts.claims();
        claims.put("user_id", userId);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
                .signWith(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }

    public String createRefreshTokenWith(Long userId) {
        Claims claims = Jwts.claims();
        claims.put("user_id", userId);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_TIME * 14))
                .signWith(hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }
}
