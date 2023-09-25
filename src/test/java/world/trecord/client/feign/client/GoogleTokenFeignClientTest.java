package world.trecord.client.feign.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import world.trecord.client.feign.client.request.GoogleTokenRequest;
import world.trecord.client.feign.client.response.GoogleTokenResponse;
import world.trecord.exception.CustomException;
import world.trecord.infra.test.AbstractIntegrationTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@AutoConfigureWireMock(port = 8089)
class GoogleTokenFeignClientTest extends AbstractIntegrationTest {

    @BeforeEach
    public void setup() {
        reset(); // reset all wireMock stubs and scenarios
    }

    @Test
    @DisplayName("올바른 요청 파라미터로 구글 토큰 서버로 요청을 하면 엑세스 토큰을 반환받는다")
    public void googleTokenFeignClientCallWithValidCodeTest() {
        //given
        stubFor(WireMock.post("/token")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"sample_token\"}")));

        GoogleTokenRequest request = new GoogleTokenRequest();

        //when
        GoogleTokenResponse response = googleTokenFeignClient.requestToken(request);

        //then
        Assertions.assertThat(response.getAccessToken()).isEqualTo("sample_token");
    }

    @Test
    @DisplayName("올바르지 않은 요청 파라미터로 구글 토큰 서버로 요청을 하면 에러가 발생한다")
    public void googleTokenFeignClientCallWithInvalidCodeTest() {
        //given
        String responseBody = null;
        stubFor(WireMock.post("/token")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        GoogleTokenRequest request = new GoogleTokenRequest();

        //when //then
        Assertions.assertThatThrownBy(() -> googleTokenFeignClient.requestToken(request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }
}
