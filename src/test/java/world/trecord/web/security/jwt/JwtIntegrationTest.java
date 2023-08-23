package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;

@IntegrationTestSupport
class JwtIntegrationTest {

    @Autowired
    JwtGenerator jwtGenerator;

    @Autowired
    private JwtParser jwtParser;

    @Test
    @DisplayName("유효한 userId로 토큰을 생성하고 그 토큰으로 userId를 정상적으로 추출한다")
    void generateAndExtractTokenTest() {
        //given
        Long originalUserId = 123L;

        //when
        String token = jwtGenerator.generateToken(originalUserId);
        Long extractedUserId = Long.valueOf(jwtParser.extractUserIdFrom(token));

        //then
        Assertions.assertThat(extractedUserId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효한 userId로 리프레시 토큰을 생성하고 그 토큰으로 userId를 정상적으로 추출한다")
    void generateAndExtractRefreshTokenTest() {
        //given
        Long originalUserId = 123L;

        //when
        String refreshToken = jwtGenerator.generateRefreshToken(originalUserId);
        Long extractedUserId = Long.valueOf(jwtParser.extractUserIdFrom(refreshToken));

        //then
        Assertions.assertThat(extractedUserId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 JwtException을 발생시킨다")
    void invalidTokenThrowsJwtException() {
        // given
        String invalidToken = "invalidToken";

        // then
        Assertions.assertThatThrownBy(() -> jwtParser.extractUserIdFrom(invalidToken))
                .isInstanceOf(JwtException.class);
    }
}