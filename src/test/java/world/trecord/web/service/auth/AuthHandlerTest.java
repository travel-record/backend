package world.trecord.web.service.auth;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.security.JwtProvider;
import world.trecord.web.security.JwtResolver;
import world.trecord.web.service.auth.google.GoogleAuthManager;
import world.trecord.web.service.auth.response.LoginResponse;
import world.trecord.web.service.auth.response.RefreshResponse;
import world.trecord.web.service.users.UserService;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AuthHandlerTest {

    @Mock
    GoogleAuthManager googleAuthManager;

    @Spy
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    JwtResolver jwtResolver;

    @InjectMocks
    AuthHandler authHandler;

    @Test
    @DisplayName("유효한 구글 인가 코드로 사용자 정보와 토큰을 반환한다")
    void googleLoginWithValidAccessTokenTest() throws Exception {
        //given
        String redirectionUri = "redirection uri";
        String accessToken = "dummy access token";
        String nickname = "nickname";
        String token = "testToken";
        String refreshToken = "testRefreshToken";

        BDDMockito.given(googleAuthManager.getUserEmail(anyString(), anyString()))
                .willReturn("test@email.com");

        BDDMockito.given(userRepository.findByEmail(anyString()))
                .willReturn(UserEntity.builder()
                        .nickname(nickname)
                        .build());

        BDDMockito.given(jwtProvider.createTokenWith(null))
                .willReturn(token);

        BDDMockito.given(jwtProvider.createRefreshTokenWith(null))
                .willReturn(refreshToken);

        //when
        LoginResponse loginResponse = authHandler.googleLogin(accessToken, redirectionUri);

        //then
        Assertions.assertThat(loginResponse.getUser().getNickname()).isEqualTo(nickname);
        Assertions.assertThat(loginResponse.getToken().getToken()).isEqualTo(token);
        Assertions.assertThat(loginResponse.getToken().getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("유효하지 않는 구글 인가 코드는 예외가 발생한다")
    void googleLoginWithInvalidAccessTokenTest() throws Exception {
        //given
        String authorizationCode = "dummy access token";
        String redirectionUri = "dummy redirection uri";
        BDDMockito.given(googleAuthManager.getUserEmail(anyString(), anyString()))
                .willThrow(new CustomException(CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE));

        //when // then
        Assertions.assertThatThrownBy(() -> authHandler.googleLogin(authorizationCode, redirectionUri))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로만 재발급하여 반환한다")
    void reissueTokenWithValidRefreshTokenTest() throws Exception {
        //given
        Long userId = 1L;
        String token = "testToken";
        String refreshToken = "testRefreshToken";

        BDDMockito.given(jwtResolver.extractUserIdFrom(anyString()))
                .willReturn(String.valueOf(userId));

        BDDMockito.given(jwtProvider.createTokenWith(userId))
                .willReturn(token);

        BDDMockito.given(jwtProvider.createRefreshTokenWith(userId))
                .willReturn(refreshToken);

        //when
        RefreshResponse refreshResponse = authHandler.reissueTokenWith(refreshToken);

        //then
        Assertions.assertThat(refreshResponse.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰은 예외가 발생한다")
    void reissueTokenWithInvalidRefreshTokenTest() throws Exception {
        //given
        String invalidToken = "dummy";

        Mockito.doThrow(new JwtException("Invalid Token"))
                .when(jwtResolver).validate(invalidToken);

        //when //then
        Assertions.assertThatThrownBy(() -> authHandler.reissueTokenWith(invalidToken))
                .isInstanceOf(JwtException.class);
    }
}