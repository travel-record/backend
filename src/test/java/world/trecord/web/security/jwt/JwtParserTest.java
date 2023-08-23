package world.trecord.web.security.jwt;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class JwtParserTest {

    @Test
    @DisplayName("유효한 토큰에서 userId를 추출한다")
    void extractUserIdFromValidTokenTest() {
        //given
        JwtParser jwtParser = new JwtParser();
        JwtGenerator jwtGenerator = new JwtGenerator();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

        Long originalUserId = 123L;
        long expiredTimeMs = 86400000L;

        String token = jwtGenerator.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when
        String userId = jwtParser.extractUserIdFrom(token);

        //then
        Assertions.assertThat(userId).isEqualTo(String.valueOf(originalUserId));
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 userId를 추출할 때 예외가 발생한다")
    void extractUserIdFromInvalidTokenTest() {
        //given

        JwtParser jwtParser = new JwtParser();
        JwtGenerator jwtGenerator = new JwtGenerator();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

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
        JwtParser jwtParser = new JwtParser();
        JwtGenerator jwtGenerator = new JwtGenerator();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

        Long originalUserId = 123L;
        long expiredTimeMs = 86400000L;

        String token = jwtGenerator.generateToken(originalUserId, secretKey, expiredTimeMs);

        //when //then
        jwtParser.verify(token);
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 검증하면 예외가 발생한다")
    void validateInvalidTokenTest() throws Exception {
        //given
        JwtParser jwtParser = new JwtParser();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

        //when //then

        Assertions.assertThatThrownBy(() -> jwtParser.verify("dummy"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Null을 검증하면 예외가 발생한다")
    void verifyWithNullTest() throws Exception {
        //when //then
        JwtParser jwtParser = new JwtParser();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

        Assertions.assertThatThrownBy(() -> jwtParser.verify(null))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("요청 메시지에 Authorization 헤더가 없으면 null을 반환한다 ")
    void extractUserIdFromHttpRequestTest() throws Exception {
        //given
        JwtParser jwtParser = new JwtParser();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

        MockHttpServletRequest request = new MockHttpServletRequest();

        //when
        Long userId = jwtParser.extractUserIdFrom(request);

        //then
        Assertions.assertThat(userId).isNull();
    }

    @Test
    @DisplayName("요청 메시지에 Authorization 헤더가 있으면 토큰을 검증하고 유효한 토큰이면 userId를 반환한다")
    void extractUserIdFromValidTokenWithRequestTest() throws Exception {
        //given
        JwtParser jwtParser = new JwtParser();
        JwtGenerator jwtGenerator = new JwtGenerator();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);
        Long userId = 1L;
        long expiredTimeMs = 86400000L;

        String token = jwtGenerator.generateToken(userId, secretKey, expiredTimeMs);

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
        JwtParser jwtParser = new JwtParser();

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        ReflectionTestUtils.setField(jwtParser, "secretKey", secretKey);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "dummy");

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.extractUserIdFrom(request))
                .isInstanceOf(JwtException.class);
    }

    // TODO 만료 시간 테스트
}
