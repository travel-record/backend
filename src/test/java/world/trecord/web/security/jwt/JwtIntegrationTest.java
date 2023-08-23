package world.trecord.web.security.jwt;

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

        //when
        String token = jwtGenerator.generateToken(originalUserId, secretKey, expiredTimeMs);
        Long extractedUserId = Long.valueOf(jwtParser.extractUserIdFrom(token));

        //then
        Assertions.assertThat(extractedUserId).isEqualTo(originalUserId);
    }
}