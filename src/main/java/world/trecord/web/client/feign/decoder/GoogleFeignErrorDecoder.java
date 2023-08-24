package world.trecord.web.client.feign.decoder;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import world.trecord.web.exception.CustomException;

import java.io.IOException;

import static world.trecord.web.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

public class GoogleFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.BAD_REQUEST.value()) {
            throw new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE);
        }

        throw new IllegalStateException(getErrorContent(response));
    }

    private String getErrorContent(Response response) {
        try {
            return Util.toString(response.body().asReader());
        } catch (IOException e) {
            return "Failed to decode error content";
        }
    }
}
