package world.trecord.web.client.feign.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.Feign;
import feign.FeignException;
import feign.Headers;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import world.trecord.web.client.feign.client.request.GoogleTokenRequest;
import world.trecord.web.client.feign.client.response.GoogleTokenResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

class GoogleTokenFeignClientTest {

    /**
     * Issue: WireMockTest는 FeignClient 테스트를 하기 위해서 일반 Feign 라이브러리를 사용해야 한다.
     * WireMock을 사용하는 테스트는 대체로 ApplicationContext 없이 Feign 클라이언트를 동작시키려는 의도로 작성하기 때문이다
     * Spring Cloud Feign의 @FeignClient와 일반 Feign 라이브러리를 함께 사용하면 충돌이 발생하여 스프링부트 컨테이너가 띄워지지 않는다
     * <p>
     * Solution: 테스트를 위해 일반 Feign 라이브러리를 사용하는 인터페이스 생성
     */
    private interface TestGoogleTokenFeignClient {

        @RequestLine("POST /token")
        @Headers("Content-Type: application/json")
        GoogleTokenResponse call(GoogleTokenRequest request);
    }

    private WireMockServer wireMockServer;
    private TestGoogleTokenFeignClient client;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        client = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(TestGoogleTokenFeignClient.class, "http://localhost:8089");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("올바른 인가 코드로 구글 서버로 요청하면 엑세스 토큰을 반환받는다")
    void googleTokenFeignClientCallWithValidCodeTest() {
        //given
        WireMock.stubFor(WireMock.post("/token")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"sample_token\"}")));

        GoogleTokenRequest request = new GoogleTokenRequest();

        //when
        GoogleTokenResponse response = client.call(request);

        //then
        Assertions.assertThat(response.getAccessToken()).isEqualTo("sample_token");
    }

    @Test
    @DisplayName("올바르지 않은 인가 코드로 구글 서버로 요청하면 예외가 발생한다")
    void googleTokenFeignClientCallWithInvalidCodeTest() {
        //given
        WireMock.stubFor(WireMock.post("/token")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"invalid_request\"}")));

        GoogleTokenRequest request = new GoogleTokenRequest(); // Configure the request accordingly

        //when //then
        Assertions.assertThatThrownBy(() -> client.call(request))
                .isInstanceOf(FeignException.class);
    }
}
