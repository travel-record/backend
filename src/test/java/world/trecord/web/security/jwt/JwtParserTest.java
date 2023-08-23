package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtParserTest {

    private JwtParser jwtParser;
    private JwtGenerator jwtGenerator;

    @BeforeEach
    void setUp() {
        jwtParser = new JwtParser();
        jwtGenerator = new JwtGenerator();
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtGenerator, "secretKey", secretKey);
    }

    @Test
    @DisplayName("유효한 토큰에서 userId를 추출한다")
    void extractUserIdFromValidTokenTest() {
        //given
        Long originalUserId = 123L;
        String token = jwtGenerator.generateToken(originalUserId);

        //when
        String userId = jwtParser.extractUserIdFrom(token);

        //then
        Assertions.assertThat(userId).isEqualTo(String.valueOf(originalUserId));
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 userId를 추출할 때 예외가 발생한다")
    void extractUserIdFromInvalidTokenTest() {
        //given
        String invalidToken = "invalidToken";

        //when //then
        Assertions.assertThatThrownBy(() -> {
                    jwtParser.extractUserIdFrom(invalidToken);
                })
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("유효한 토큰을 검증하면 예외가 발생하지 않는다")
    void validateValidTokenTest() throws Exception {
        //given
        Long originalUserId = 123L;
        String token = jwtGenerator.generateToken(originalUserId);

        //when //then
        jwtParser.verify(token);
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 검증하면 예외가 발생한다")
    void validateInvalidTokenTest() throws Exception {
        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.verify("dummy"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Null을 검증하면 예외가 발생한다")
    void verifyWithNullTest() throws Exception {
        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.verify(null))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("요청 메시지에 Authorization 헤더가 없으면 null을 반환한다 ")
    void extractUserIdFromHttpRequestTest() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();

        //when
        Long userId = jwtParser.extractUserIdFrom(request);

        //then
        Assertions.assertThat(userId).isNull();
    }

    @Test
    @DisplayName("요청 메시지에 Authorization 헤더가 있으면 토큰을 검증하고 유요한 토큰이면 userId를 반환한다")
    void extractUserIdFromValidTokenWithRequestTest() throws Exception {
        //given
        Long userId = 1L;
        String token = jwtGenerator.generateToken(userId);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", token);

        //when
        Long extractId = jwtParser.extractUserIdFrom(request);

        //then
        Assertions.assertThat(userId).isEqualTo(extractId);
    }

    @Test
    @DisplayName("요청 메시지에 Authorization 헤더가 있으면 토큰을 검증하고 유효하지 않은 토큰이면 예외가 발생한다")
    void extractUserIdFromInvalidTokenWithRequestTest() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "dummy");

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.extractUserIdFrom(request))
                .isInstanceOf(JwtException.class);
    }

    // TODO 만료 시간 테스트
}
