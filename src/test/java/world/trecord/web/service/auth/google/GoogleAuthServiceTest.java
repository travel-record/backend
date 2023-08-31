package world.trecord.web.service.auth.google;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import world.trecord.web.client.feign.client.GoogleTokenFeignClient;
import world.trecord.web.client.feign.client.GoogleUserInfoFeignClient;
import world.trecord.web.client.feign.client.request.GoogleTokenRequest;
import world.trecord.web.client.feign.client.response.GoogleTokenResponse;
import world.trecord.web.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.web.exception.CustomException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static world.trecord.web.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private GoogleTokenFeignClient googleTokenFeignClient;

    @Mock
    private GoogleUserInfoFeignClient googleUserInfoFeignClient;

    @Test
    @DisplayName("올바른 인가 코드와 리디렉션 URI로 구글 서버로 요청하여 사용자의 정보를 얻어와서 반환한다")
    void getUserEmailWithValidAuthorizationCodeTest() {
        //given
        String authorizationCode = "testCode";
        String redirectionUri = "http://test.com";
        String mockAccessToken = "mockToken";
        String expectedEmail = "test@test.com";

        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse();
        mockTokenResponse.setAccess_token(mockAccessToken);

        GoogleUserInfoResponse mockUserInfoResponse = new GoogleUserInfoResponse();
        mockUserInfoResponse.setEmail(expectedEmail);

        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class))).thenReturn(ResponseEntity.ok(mockTokenResponse));
        when(googleUserInfoFeignClient.fetchUserInfo("Bearer " + mockAccessToken)).thenReturn(ResponseEntity.ok(mockUserInfoResponse));

        //when
        String resultEmail = googleAuthService.getUserEmail(authorizationCode, redirectionUri);

        //then
        Assertions.assertThat(expectedEmail).isEqualTo(resultEmail);
    }

    @Test
    @DisplayName("올바르지 않은 인가 코드와 리디렉션 URI로 구글 서버로 요청하면 예외를 던진다")
    void getUserEmailWithInvalidAuthorizationCodeTest() {
        //given
        String authorizationCode = "testCode";
        String redirectionUri = "http://test.com";

        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class))).thenReturn(ResponseEntity.ok(null));

        //when //then
        Assertions.assertThatThrownBy(() -> googleAuthService.getUserEmail(authorizationCode, redirectionUri))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }

    @Test
    @DisplayName("엑세스 토큰으로 사용자 정보를 얻어오지 못하면 예외를 던진다")
    void getUserEmailWithInvalidAccessTokenTest() {
        //given
        String authorizationCode = "testCode";
        String redirectionUri = "http://test.com";
        String mockAccessToken = "mockToken";

        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse();
        mockTokenResponse.setAccess_token(mockAccessToken);

        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class))).thenReturn(ResponseEntity.ok(mockTokenResponse));
        when(googleUserInfoFeignClient.fetchUserInfo("Bearer " + mockAccessToken)).thenReturn(ResponseEntity.ok(null));

        //when //then
        Assertions.assertThatThrownBy(() -> googleAuthService.getUserEmail(authorizationCode, redirectionUri))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }
}