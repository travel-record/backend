package world.trecord.web.security;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import world.trecord.IntegrationTestSupport;

@IntegrationTestSupport
class JwtParserTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtParser jwtParser;

    @Test
    @DisplayName("유효한 토큰으로 유저 ID를 추출할 수 있다")
    public void extractUserIdFromTokenTest() {
        // given
        Long originalUserId = 123L;
        String token = jwtProvider.createTokenWith(originalUserId);

        // when
        Long extractedUserId = Long.parseLong(jwtParser.extractUserIdFrom(token));

        // then
        Assertions.assertThat(originalUserId).isEqualTo(extractedUserId);
    }

    @Test
    @DisplayName("유효한 토큰을 검증하면 아무것도 반환하지 않는다")
    void validateValidTokenTest() throws Exception {
        //given
        Long originalUserId = 123L;
        String token = jwtProvider.createTokenWith(originalUserId);

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
    void extractUserIdFromValidTokenTest() throws Exception {
        //given
        Long userId = 1L;
        String token = jwtProvider.createTokenWith(userId);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", token);

        //when
        Long extractId = jwtParser.extractUserIdFrom(request);

        //then
        Assertions.assertThat(userId).isEqualTo(extractId);
    }

    @Test
    @DisplayName("요청 메시지에 Authorization 헤더가 있으면 토큰을 검증하고 유효하지 않은 토큰이면 예외가 발생한다")
    void extractUserIdFromInvalidTokenTest() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "dummy");

        //when //then
        Assertions.assertThatThrownBy(() -> jwtParser.extractUserIdFrom(request))
                .isInstanceOf(JwtException.class);
    }

}