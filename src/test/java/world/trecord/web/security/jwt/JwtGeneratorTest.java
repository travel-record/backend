package world.trecord.web.security.jwt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtGeneratorTest {

    private JwtGenerator jwtGenerator;

    @BeforeEach
    void setUp() {
        jwtGenerator = new JwtGenerator();
        ReflectionTestUtils.setField(jwtGenerator, "secretKey", "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE");
    }

    @Test
    @DisplayName("userId로 토큰을 생성한다")
    void generateTokenTest() {
        //given
        Long userId = 123L;
        String tokenPattern = "^[a-zA-Z0-9-_]+(=)*$";

        //when
        String token = jwtGenerator.generateToken(userId);

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
    @DisplayName("userId로 리프레시 토큰을 생성한다")
    void generateRefreshTokenTest() {
        //given
        Long userId = 123L;
        String tokenPattern = "^[a-zA-Z0-9-_]+(=)*$";

        //when
        String token = jwtGenerator.generateRefreshToken(userId);

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