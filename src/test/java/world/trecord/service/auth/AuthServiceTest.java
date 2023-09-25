package world.trecord.service.auth;

import io.jsonwebtoken.JwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.redis.UserCacheRepository;
import world.trecord.config.security.JwtTokenHandler;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.auth.response.LoginResponse;
import world.trecord.dto.auth.response.RefreshResponse;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.test.AbstractMockMvcTest;
import world.trecord.service.users.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AuthServiceTest extends AbstractMockMvcTest {

    @Mock
    GoogleAuthService googleAuthService;

    @Spy
    UserRepository userRepository;

    @Mock
    JwtTokenHandler jwtTokenHandler;

    @Mock
    UserService userService;

    @Mock
    JwtProperties jwtProperties;

    @Mock
    UserCacheRepository userCacheRepository;

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
        Long userId = 1L;

        given(jwtProperties.getSecretKey())
                .willReturn("secret key");

        given(jwtProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(googleAuthService.getUserEmail(anyString(), anyString()))
                .willReturn("test@email.com");

        UserEntity userEntity = UserEntity.builder()
                .nickname(nickname)
                .build();

        ReflectionTestUtils.setField(userEntity, "id", userId);

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.ofNullable(userEntity));

        given(jwtTokenHandler.generateToken(anyLong(), anyString(), anyLong()))
                .willReturn(token);

        //when
        LoginResponse loginResponse = authService.googleLogin(accessToken, redirectionUri);

        //then
        Assertions.assertThat(loginResponse)
                .extracting("user.nickname", "token.token", "token.refreshToken")
                .containsExactly(nickname, token, token);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 새로운 사용자 생성한다")
    void googleLoginWithNonExistingEmailTest() {
        //given
        String redirectionUri = "redirection uri";
        String accessToken = "dummy access token";

        given(jwtProperties.getSecretKey())
                .willReturn("secret key");

        given(jwtProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(googleAuthService.getUserEmail(anyString(), anyString()))
                .willReturn("nonexisting@email.com");

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        given(userService.createUser(anyString()))
                .willReturn(UserEntity.builder().email("nonexisting@email.com").nickname("newUser").build());

        //when
        LoginResponse loginResponse = authService.googleLogin(accessToken, redirectionUri);

        //then
        Mockito.verify(userService, times(1)).createUser(anyString());
    }

    @Test
    @DisplayName("JWT 토큰 생성 실패 시 예외가 발생한다")
    void generateTokenFailureTest() {
        //given
        String secretKey = "secret key";
        given(jwtProperties.getSecretKey())
                .willReturn(secretKey);

        given(jwtProperties.getTokenExpiredTimeMs())
                .willReturn(87654321L);

        given(googleAuthService.getUserEmail(anyString(), anyString()))
                .willReturn("nonexisting@email.com");

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        UserEntity userEntity = UserEntity.builder()
                .email("nonexisting@email.com")
                .nickname("newUser")
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1L);

        given(userService.createUser(anyString()))
                .willReturn(userEntity);

        given(jwtTokenHandler.generateToken(eq(userEntity.getId()), anyString(), anyLong()))
                .willThrow(new JwtException("Token generation failed"));

        //when // then
        Assertions.assertThatThrownBy(() -> authService.googleLogin("dummy", "dummy"))
                .isInstanceOf(JwtException.class);
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
        String secretKey = "secret key";
        given(jwtProperties.getSecretKey())
                .willReturn(secretKey);

        long expiredTimeMs = 87654321L;
        given(jwtProperties.getTokenExpiredTimeMs())
                .willReturn(expiredTimeMs);

        long userId = 1L;
        String token = "testToken";
        given(jwtTokenHandler.getUserIdFromToken(secretKey, token))
                .willReturn(userId);

        given(jwtTokenHandler.generateToken(eq(userId), anyString(), eq(expiredTimeMs)))
                .willReturn(token);

        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getId()).thenReturn(userId);
        when(userRepository.findById(any())).thenReturn(Optional.of(mockUser));

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
        String secretKey = "secret key";
        given(jwtProperties.getSecretKey())
                .willReturn(secretKey);

        doThrow(new JwtException("Invalid Token"))
                .when(jwtTokenHandler).verifyToken(secretKey, invalidToken);

        //when //then
        Assertions.assertThatThrownBy(() -> authService.reissueToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("토큰에서 추출한 유저가 조회되지 않으면 예외가 발생한다")
    void reissueTokenWhenUserNotFoundTest() throws Exception {
        //given
        long userId = 1L;
        String token = "testToken";
        String secretKey = "secret key";

        given(jwtProperties.getSecretKey())
                .willReturn(secretKey);

        given(jwtTokenHandler.getUserIdFromToken(secretKey, token))
                .willReturn(userId);

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //when //then
        Assertions.assertThatThrownBy(() -> authService.reissueToken(token))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }
}