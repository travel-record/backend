package world.trecord.web.feign.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import world.trecord.exception.CustomException;

import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

public class GoogleFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        throw new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE);
    }
}
