package world.trecord.web.controller.auth;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.controller.auth.request.GoogleLoginRequest;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.auth.AuthHandler;
import world.trecord.web.service.auth.response.LoginResponse;

@ExtendWith(MockitoExtension.class)
class AuthControllerMockTest {

    @Mock
    AuthHandler authHandler;

    @InjectMocks
    AuthController authController;

    @Test
    @DisplayName("POST /google-login - 성공")
    void googleLoginWithValidAccessTokenTest() throws Exception {
        //given
        String redirectionUri = "redirection uri";
        String validAuthorizationCode = "test authorization code";
        String token = "test token";
        String refreshToken = "test refresh token";
        String nickname = "nickname";
        long userId = 1L;

        GoogleLoginRequest request = GoogleLoginRequest.builder().authorizationCode(validAuthorizationCode).redirectionUri(redirectionUri).build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .token(token)
                .refreshToken(refreshToken)
                .build();

        BDDMockito.when(authHandler.googleLogin(validAuthorizationCode, redirectionUri))
                .thenReturn(loginResponse);

        //when
        ApiResponse<LoginResponse> apiResponse = authController.googleLogin(request);

        // then
        Assertions.assertThat(apiResponse.getData().getUser())
                .extracting("userId", "nickname")
                .containsExactly(userId, nickname);

        Assertions.assertThat(apiResponse.getData().getToken())
                .extracting("token", "refreshToken")
                .containsExactly(token, refreshToken);
    }

    @Test
    @DisplayName("POST /google-login - 실패 (유효하지 않는 구글 인가 코드로 요청)")
    void googleLoginWithInvalidAccessTokenTest() throws Exception {
        //given
        String redirectionUri = "redirection uri";
        String validAuthorizationCode = "test authorization code";

        GoogleLoginRequest request = GoogleLoginRequest.builder().authorizationCode(validAuthorizationCode).redirectionUri(redirectionUri).build();

        BDDMockito.when(authHandler.googleLogin(validAuthorizationCode, redirectionUri))
                .thenThrow(CustomException.class);

        //when //then
        Assertions.assertThatThrownBy(() -> authController.googleLogin(request))
                .isInstanceOf(CustomException.class);
    }

}