package world.trecord.web.client.feign.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import world.trecord.IntegrationTestSupport;
import world.trecord.web.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.web.exception.CustomException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static world.trecord.web.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@AutoConfigureWireMock(port = 8089)
@IntegrationTestSupport
class GoogleUserInfoFeignClientTest {

    @Autowired
    private GoogleUserInfoFeignClient client;

    @Test
    @DisplayName("올바른 엑세스 토큰으로 구글 서버에 요청하면 사용자 정보를 반환받는다")
    public void googleTokenFeignClientCallWithValidCodeTest() {
        //given
        String validToken = "valid token";
        String responseBody = "{\"email\":\"sample@gmail.com\"}";

        stubFor(WireMock.get("/oauth2/v3/userinfo")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        //when
        GoogleUserInfoResponse response = client.fetchUserInfo(validToken);

        //then
        Assertions.assertThat(response.getEmail()).isEqualTo("sample@gmail.com");
    }

    @Test
    @DisplayName("올바르지 않은 엑세스 토큰으로 구글 서버에 요청하면 예외가 발생한다")
    public void googleTokenFeignClientCallWithInvalidCodeTest() {
        //given
        String invalidToken = "invalid token";
        String responseBody = null;

        stubFor(WireMock.get("/oauth2/v3/userinfo")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        //when //then
        Assertions.assertThatThrownBy(() -> client.fetchUserInfo("Bearer " + invalidToken))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }

}