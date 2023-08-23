package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtParserTest {

    @Test
    @DisplayName("유효한 토큰에서 userId를 추출한다")
    void extractUserIdFromValidTokenTest() {
        //given
        JwtParser jwtParser = new JwtParser();
        JwtGenerator jwtGenerator = new JwtGenerator();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        Long originalUserId = 123L;
        long expiredTimeMs = 86400000L;

        String token = jwtGenerator.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        String userId = jwtParser.extractUserId(secretKey, token);

        //then
        Assertions.assertThat(userId).isEqualTo(String.valueOf(originalUserId));
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 userId를 추출할 때 예외가 발생한다")
    void extractUserIdFromInvalidTokenTest() {
        //given
        JwtParser jwtParser = new JwtParser();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        String invalidToken = "invalidToken";

        //when //then
        Assertions.assertThatThrownBy(() -> {
                    jwtParser.extractUserId(secretKey, invalidToken);
                })
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("유효한 토큰을 검증하면 예외가 발생하지 않는다")
    void validateValidTokenTest() throws Exception {
        //given
        JwtParser jwtParser = new JwtParser();
        JwtGenerator jwtGenerator = new JwtGenerator();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        Long originalUserId = 123L;
        long expiredTimeMs = 86400000L;

        String token = jwtGenerator.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when //then
        jwtParser.verify(secretKey, token);
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 검증하면 예외가 발생한다")
    void validateInvalidTokenTest() throws Exception {
        //given
        JwtParser jwtParser = new JwtParser();

        String invalidToken = "invalid token";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.verify(secretKey, invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("null을 검증하면 예외가 발생한다")
    void verifyWithNullTest() throws Exception {
        //given
        JwtParser jwtParser = new JwtParser();

        String nullToken = null;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.verify(secretKey, nullToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증시 JwtException 예외가 발생한다")
    void verifyExpiredTokenTest() {
        //given
        JwtGenerator jwtGenerator = new JwtGenerator();
        JwtParser jwtParser = new JwtParser();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = -1000L;
        long userId = 1L;

        String expiredToken = jwtGenerator.generateToken(userId, secretKey, expiredTimeMs);

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.verify(secretKey, expiredToken))
                .isInstanceOf(JwtException.class);
    }
}
