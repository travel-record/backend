package world.trecord.web.client.feign.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.*;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import world.trecord.web.client.feign.client.response.GoogleUserInfoResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class GoogleUserInfoFeignClientTest {

    /**
     * Issue: WireMockTest는 FeignClient 테스트를 하기 위해서 일반 Feign 라이브러리를 사용해야 한다.
     * WireMock을 사용하는 테스트는 대체로 ApplicationContext 없이 Feign 클라이언트를 동작시키려는 의도로 작성하기 때문이다
     * Spring Cloud Feign의 @FeignClient와 일반 Feign 라이브러리를 함께 사용하면 충돌이 발생하여 스프링부트 컨테이너가 띄워지지 않는다
     * <p>
     * Solution: 테스트를 위해 일반 Feign 라이브러리를 사용하는 인터페이스 생성
     */
    private interface TestGoogleUserInfoFeignClient {

        @RequestLine("GET /oauth2/v3/userinfo")
        @Headers("Authorization: Bearer {accessToken}")
        GoogleUserInfoResponse call(@Param("accessToken") String accessToken);
    }

    private WireMockServer wireMockServer;
    private TestGoogleUserInfoFeignClient client;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        client = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(TestGoogleUserInfoFeignClient.class, "http://localhost:8089");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("올바른 엑세스 토큰으로 구글 서버에 요청하면 사용자 정보를 반환받는다")
    void callWithValidTokenTest() {
        // given
        String validToken = "valid token";
        String responseBody = "{\"email\":\"sample@gmail.com\"}";

        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/oauth2/v3/userinfo"))
                .withHeader("Authorization", equalTo("Bearer " + validToken))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // when
        GoogleUserInfoResponse response = client.call(validToken);

        // then
        Assertions.assertThat(response.getEmail()).isEqualTo("sample@gmail.com"); // 적절한 검증 로직으로 수정하세요
    }

    @Test
    @DisplayName("올바르지 않은 엑세스 토큰으로 구글 서버에 요청하면 예외가 발생한다")
    void callWithInvalidTokenTest() {
        // given
        String invalidToken = "invalid token";
        String responseBody = null;

        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/oauth2/v3/userinfo"))
                .withHeader("Authorization", equalTo("Bearer " + invalidToken))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // when // then
        Assertions.assertThatThrownBy(() -> client.call(invalidToken))
                .isInstanceOf(FeignException.class);
    }
}