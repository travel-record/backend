package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import world.trecord.IntegrationTestSupport;

@IntegrationTestSupport
class JwtIntegrationTest {

    @Autowired
    JwtGenerator jwtGenerator;

    @Autowired
    private JwtParser jwtParser;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    @Test
    @DisplayName("유효한 userId로 토큰을 생성하고 그 토큰으로 userId를 정상적으로 추출한다")
    void generateAndExtractTokenTest() {
        //given
        Long originalUserId = 123L;
        String token = jwtGenerator.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        Long extractedUserId = Long.valueOf(jwtParser.extractUserId(secretKey, token));

        //then
        Assertions.assertThat(extractedUserId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효하지 않는 토큰으로 userId를 추출하려고 하면 JwtException이 발생한다")
    void extractUserIdWithInvalidToken() throws Exception {
        //given
        String invalidToken = "-1";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.extractUserId(secretKey, invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 토큰으로 userId를 추출하려고 하면 JwtException이 발생한다")
    void extractUserIdWithExpiredToken() throws Exception {
        //given
        Long originalUserId = 123L;
        String expiredToken = jwtGenerator.generateToken(originalUserId, secretKey, -1000L);

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.extractUserId(secretKey, expiredToken))
                .isInstanceOf(JwtException.class);
    }

}