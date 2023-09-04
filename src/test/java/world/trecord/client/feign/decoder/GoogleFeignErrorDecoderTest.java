package world.trecord.client.feign.decoder;

import feign.Request;
import feign.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.client.feign.decoder.GoogleFeignErrorDecoder;
import world.trecord.exception.CustomException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

class GoogleFeignErrorDecoderTest {

    @Test
    @DisplayName("응답이 400이면 CustomException 예외를 던진다")
    void decodeTestWhenResponseIs400() {
        //given
        GoogleFeignErrorDecoder decoder = new GoogleFeignErrorDecoder();

        Request request = Request.create(Request.HttpMethod.GET, "http://test.com", Collections.emptyMap(), Request.Body.empty(), new feign.RequestTemplate());

        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .headers(Collections.emptyMap())
                .body("Error Message", StandardCharsets.UTF_8)
                .request(request)
                .build();

        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> decoder.decode("methodKey", response)
        );

        //then
        Assertions.assertThat(exception)
                .extracting("error")
                .isEqualTo(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }

    @Test
    @DisplayName("응답이 500이면 IllegalStateException 예외를 던진다")
    void decodeTestWhenResponseIs500() {
        //given
        GoogleFeignErrorDecoder decoder = new GoogleFeignErrorDecoder();

        Request request = Request.create(Request.HttpMethod.GET, "http://test.com", Collections.emptyMap(), Request.Body.empty(), new feign.RequestTemplate());

        String errorMessage = "Internal Server Error";

        Response response = Response.builder()
                .status(500)
                .reason("Server Error")
                .headers(Collections.emptyMap())
                .body(errorMessage, StandardCharsets.UTF_8)
                .request(request)
                .build();

        //when
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> decoder.decode("methodkey", response)
        );

        //then
        Assertions.assertThat(exception.getMessage())
                .isEqualTo(errorMessage);
    }
}