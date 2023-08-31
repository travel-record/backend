package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenHandlerTest {

    @Test
    @DisplayName("유효한 토큰에서 userId를 추출한다")
    void extractUserIdFromValidTokenTest() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        Long originalUserId = 123L;
        long expiredTimeMs = 86400000L;

        String token = jwtTokenHandler.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        Long userId = jwtTokenHandler.extractUserId(secretKey, token);

        //then
        Assertions.assertThat(userId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 userId를 추출할 때 예외가 발생한다")
    void extractUserIdFromInvalidTokenTest() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        String invalidToken = "invalidToken";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> {
                    jwtTokenHandler.extractUserId(secretKey, invalidToken);
                })
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("유효한 토큰을 검증하면 예외가 발생하지 않는다")
    void validateValidTokenTest() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        Long originalUserId = 123L;
        long expiredTimeMs = 86400000L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        String token = jwtTokenHandler.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when //then
        jwtTokenHandler.verify(secretKey, token);
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 검증하면 예외가 발생한다")
    void validateInvalidTokenTest() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        String invalidToken = "invalid token";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.verify(secretKey, invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("null을 검증하면 JwtException 예외가 발생한다")
    void verifyWithNullTest() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        String nullToken = null;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.verify(secretKey, nullToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증시 JwtException 예외가 발생한다")
    void verifyExpiredTokenTest() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        long userId = 1L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = -1000L;

        String expiredToken = jwtTokenHandler.generateToken(userId, secretKey, expiredTimeMs);

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.verify(secretKey, expiredToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("userId로 토큰을 생성한다")
    void generateTokenTest() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        Long userId = 123L;

        String tokenPattern = "^[a-zA-Z0-9-_]+(=)*$";

        //when
        String token = jwtTokenHandler.generateToken(userId, secretKey, expiredTimeMs);

        //then
        Assertions.assertThat(token)
                .isNotNull()
                .satisfies(t -> {
                    Assertions.assertThat(t.split("\\."))
                            .hasSize(3)
                            .allMatch(part -> part.matches(tokenPattern));  // verify jwt format
                });
    }

    @Test
    @DisplayName("유효한 userId로 토큰을 생성하고 그 토큰으로 userId를 정상적으로 추출한다")
    void generateAndExtractTokenTest() {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        Long originalUserId = 123L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        String token = jwtTokenHandler.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        Long extractedUserId = Long.valueOf(jwtTokenHandler.extractUserId(secretKey, token));

        //then
        Assertions.assertThat(extractedUserId).isEqualTo(originalUserId);
    }

    @Test
    @DisplayName("유효하지 않는 토큰으로 userId를 추출하려고 하면 JwtException이 발생한다")
    void extractUserIdWithInvalidToken() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        String invalidToken = "-1";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.extractUserId(secretKey, invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 토큰으로 userId를 추출하려고 하면 JwtException이 발생한다")
    void extractUserIdWithExpiredToken() throws Exception {
        //given
        JwtTokenHandler jwtTokenHandler = new JwtTokenHandler();
        Long originalUserId = 123L;
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        String expiredToken = jwtTokenHandler.generateToken(originalUserId, secretKey, -1000L);

        //when //then
        Assertions.assertThatThrownBy(() -> jwtTokenHandler.extractUserId(secretKey, expiredToken))
                .isInstanceOf(JwtException.class);
    }
}
