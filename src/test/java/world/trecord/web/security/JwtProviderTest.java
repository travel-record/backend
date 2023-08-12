package world.trecord.web.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;

@IntegrationTestSupport
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Test
    @DisplayName("userId로 토큰을 생성한다")
    void createTokenWithUserIdTest() throws Exception {
        //given
        Long originalUserId = 123L;

        //when
        String token = jwtProvider.createTokenWith(originalUserId);

        //then
        Assertions.assertThat(token).isNotEqualTo(originalUserId);
    }

    @Test
    @DisplayName("userId로 리프레시 토큰을 생성한다")
    void createRefreshTokenWithUserIdTest() throws Exception {
        //given
        Long originalUserId = 123L;

        //when
        String token = jwtProvider.createRefreshTokenWith(originalUserId);

        //then
        Assertions.assertThat(token).isNotEqualTo(originalUserId);
    }

}