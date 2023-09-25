package world.trecord.service.auth;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import world.trecord.client.feign.client.GoogleTokenFeignClient;
import world.trecord.client.feign.client.GoogleUserInfoFeignClient;
import world.trecord.client.feign.client.request.GoogleTokenRequest;
import world.trecord.client.feign.client.response.GoogleTokenResponse;
import world.trecord.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.config.properties.GoogleProperties;
import world.trecord.exception.CustomException;
import world.trecord.infra.test.AbstractMockMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

class GoogleAuthServiceTest extends AbstractMockMvcTest {

    @InjectMocks
    GoogleAuthService googleAuthService;

    @Mock
    GoogleTokenFeignClient googleTokenFeignClient;

    @Mock
    GoogleUserInfoFeignClient googleUserInfoFeignClient;

    @Mock
    GoogleProperties googleProperties;


    @Test
    @DisplayName("올바른 인가 코드와 리디렉션 URI로 구글 서버로 요청하여 사용자의 정보를 얻어와서 반환한다")
    void getUserEmailWithValidAuthorizationCodeTest() {
        //given
        String authorizationCode = "testCode";
        String redirectionUri = "http://test.com";
        String mockAccessToken = "mockToken";
        String expectedEmail = "test@test.com";

        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse();
        mockTokenResponse.setAccessToken(mockAccessToken);

        GoogleUserInfoResponse mockUserInfoResponse = new GoogleUserInfoResponse();
        mockUserInfoResponse.setEmail(expectedEmail);

        when(googleProperties.getClientId()).thenReturn("client id");
        when(googleProperties.getClientSecret()).thenReturn("client secret");
        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class))).thenReturn(mockTokenResponse);
        when(googleUserInfoFeignClient.fetchUserInfo("Bearer " + mockAccessToken)).thenReturn(mockUserInfoResponse);

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

        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class))).thenReturn(null);

        //when //then
        Assertions.assertThatThrownBy(() -> googleAuthService.getUserEmail(authorizationCode, redirectionUri))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }

    @Test
    @DisplayName("Feign 호출 중 에러 발생 시 예외를 핸들링한다")
    void feignErrorHandlingTest() {
        //given
        String authorizationCode = "testCode";
        String redirectionUri = "http://test.com";

        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class)))
                .thenThrow(CustomException.class);

        //when //then
        Assertions.assertThatThrownBy(() -> googleAuthService.getUserEmail(authorizationCode, redirectionUri))
                .isInstanceOf(CustomException.class); // or whatever exception you handle the FeignException with
    }

    @Test
    @DisplayName("엑세스 토큰으로 사용자 정보를 얻어오지 못하면 예외를 던진다")
    void getUserEmailWithInvalidAccessTokenTest() {
        //given
        String authorizationCode = "testCode";
        String redirectionUri = "http://test.com";
        String mockAccessToken = "mockToken";

        GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse();
        mockTokenResponse.setAccessToken(mockAccessToken);

        when(googleTokenFeignClient.requestToken(any(GoogleTokenRequest.class))).thenReturn(mockTokenResponse);
        when(googleUserInfoFeignClient.fetchUserInfo("Bearer " + mockAccessToken)).thenReturn(null);

        //when //then
        Assertions.assertThatThrownBy(() -> googleAuthService.getUserEmail(authorizationCode, redirectionUri))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }
}