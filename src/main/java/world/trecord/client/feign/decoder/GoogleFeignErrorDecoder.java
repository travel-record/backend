package world.trecord.client.feign.decoder;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import world.trecord.exception.CustomException;

import java.nio.charset.StandardCharsets;

import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@Slf4j
public class GoogleFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (isClientRequestError(response.status())) {
            log.error("Google authorization request error detected for method {}: Response status: {}, Content: {}",
                    methodKey, response.status(), getErrorContent(response));
            throw new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE);
        }
        throw new IllegalStateException(getErrorContent(response));
    }

    private boolean isClientRequestError(int status) {
        return 400 <= status && status < 500;
    }

    private String getErrorContent(Response response) {
        if (response.body() == null) {
            return "Response error body is null";
        }

        try {
            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            return new String(bodyData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to decode error content";
        }
    }
}
