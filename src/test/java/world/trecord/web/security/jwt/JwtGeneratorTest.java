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
        Long originalUserId = 123L;

        //when
        String token = jwtGenerator.generateToken(originalUserId);

        //then
        Assertions.assertThat(token).isNotNull();

        //verify JWT format
        Assertions.assertThat(token.split("\\.").length).isEqualTo(3);
        Assertions.assertThat(token.split("\\.")[0]).matches("^[a-zA-Z0-9-_]+(=)*$");
        Assertions.assertThat(token.split("\\.")[1]).matches("^[a-zA-Z0-9-_]+(=)*$");
        Assertions.assertThat(token.split("\\.")[2]).matches("^[a-zA-Z0-9-_]+(=)*$");
    }

    @Test
    @DisplayName("userId로 리프레시 토큰을 생성한다")
    void generateRefreshTokenTest() {
        //given
        Long originalUserId = 123L;

        //when
        String token = jwtGenerator.generateRefreshToken(originalUserId);

        //then
        Assertions.assertThat(token).isNotNull();

        //verify JWT format
        Assertions.assertThat(token.split("\\.").length).isEqualTo(3);
        Assertions.assertThat(token.split("\\.")[0]).matches("^[a-zA-Z0-9-_]+(=)*$");
        Assertions.assertThat(token.split("\\.")[1]).matches("^[a-zA-Z0-9-_]+(=)*$");
        Assertions.assertThat(token.split("\\.")[2]).matches("^[a-zA-Z0-9-_]+(=)*$");
    }

}