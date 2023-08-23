package world.trecord.web.security.jwt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtGeneratorTest {

    @Test
    @DisplayName("userId로 토큰을 생성한다")
    void generateTokenTest() {
        //given
        JwtGenerator jwtGenerator = new JwtGenerator();
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        Long userId = 123L;

        String tokenPattern = "^[a-zA-Z0-9-_]+(=)*$";

        //when
        String token = jwtGenerator.generateToken(userId, secretKey, expiredTimeMs);

        //then
        Assertions.assertThat(token)
                .isNotNull()
                .satisfies(t -> {
                    Assertions.assertThat(t.split("\\."))
                            .hasSize(3)
                            .allMatch(part -> part.matches(tokenPattern));  // verify jwt format
                });
    }
}