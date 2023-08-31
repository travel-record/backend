package world.trecord.web.service.auth;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;
import world.trecord.web.service.auth.google.GoogleAuthService;
import world.trecord.web.service.auth.response.LoginResponse;
import world.trecord.web.service.auth.response.RefreshResponse;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    GoogleAuthService googleAuthService;

    @Spy
    UserRepository userRepository;

    @Mock
    JwtTokenHandler jwtTokenHandler;

    @Spy
    JwtProperties jwtProperties;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("유효한 구글 인가 코드로 사용자 정보와 토큰을 반환한다")
    void googleLoginWithValidAccessTokenTest() throws Exception {
        //given
        String redirectionUri = "redirection uri";
        String accessToken = "dummy access token";
        String nickname = "nickname";
        String token = "testToken";

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        ReflectionTestUtils.setField(jwtProperties, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtProperties, "tokenExpiredTimeMs", expiredTimeMs);

        given(googleAuthService.getUserEmail(anyString(), anyString()))
                .willReturn("test@email.com");

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.ofNullable(UserEntity.builder()
                        .nickname(nickname)
                        .build()));

        given(jwtTokenHandler.generateToken(null, secretKey, expiredTimeMs))
                .willReturn(token);

        given(jwtTokenHandler.generateToken(null, secretKey, expiredTimeMs * 14))
                .willReturn(token);

        //when
        LoginResponse loginResponse = authService.googleLogin(accessToken, redirectionUri);

        //then
        Assertions.assertThat(loginResponse)
                .extracting("user.nickname", "token.token", "token.refreshToken")
                .containsExactly(nickname, token, token);
    }

    @Test
    @DisplayName("유효하지 않는 구글 인가 코드는 예외가 발생한다")
    void googleLoginWithInvalidAccessTokenTest() throws Exception {
        //given
        String authorizationCode = "dummy access token";
        String redirectionUri = "dummy redirection uri";
        given(googleAuthService.getUserEmail(anyString(), anyString()))
                .willThrow(new CustomException(CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE));

        //when // then
        Assertions.assertThatThrownBy(() -> authService.googleLogin(authorizationCode, redirectionUri))
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
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        ReflectionTestUtils.setField(jwtProperties, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtProperties, "tokenExpiredTimeMs", expiredTimeMs);

        given(jwtTokenHandler.extractUserId(secretKey, token))
                .willReturn(userId);

        given(jwtTokenHandler.generateToken(eq(userId), anyString(), eq(expiredTimeMs)))
                .willReturn(token);

        //when
        RefreshResponse refreshResponse = authService.reissueToken(token);

        //then
        Assertions.assertThat(refreshResponse.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰은 예외가 발생한다")
    void reissueTokenWithInvalidRefreshTokenTest() throws Exception {
        //given
        String invalidToken = "dummy";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";
        long expiredTimeMs = 86400000L;

        ReflectionTestUtils.setField(jwtProperties, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtProperties, "tokenExpiredTimeMs", expiredTimeMs);

        doThrow(new JwtException("Invalid Token"))
                .when(jwtTokenHandler).verify(secretKey, invalidToken);

        //when //then
        Assertions.assertThatThrownBy(() -> authService.reissueToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }
}