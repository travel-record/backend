package world.trecord.web.service.auth.google;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import world.trecord.exception.CustomException;
import world.trecord.web.feign.client.GoogleTokenFeignClient;
import world.trecord.web.feign.client.GoogleUserInfoFeignClient;
import world.trecord.web.feign.request.GoogleTokenRequest;
import world.trecord.web.feign.response.GoogleTokenResponse;
import world.trecord.web.feign.response.GoogleUserInfoResponse;

import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@RequiredArgsConstructor
@Component
public class GoogleAuthManager {

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    private final GoogleTokenFeignClient googleTokenFeignClient;
    private final GoogleUserInfoFeignClient googleUserInfoFeignClient;

    public String getUserEmail(String authorizationCode, String redirectionUri) {
        String accessToken = requestAccessTokenWith(authorizationCode, redirectionUri);
        return requestUserEmailWith(accessToken);
    }

    private String requestAccessTokenWith(String authorizationCode, String redirectionUri) throws CustomException {
        GoogleTokenRequest request = GoogleTokenRequest.builder()
                .client_id(googleClientId)
                .client_secret(googleClientSecret)
                .code(authorizationCode)
                .redirect_uri(redirectionUri)
                .grant_type("authorization_code")
                .build();

        ResponseEntity<GoogleTokenResponse> response = googleTokenFeignClient.call(request);

        GoogleTokenResponse body = response.getBody();

        if (body == null) {
            throw new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE);
        }

        return body.getAccess_token();
    }

    private String requestUserEmailWith(String accessToken) {
        ResponseEntity<GoogleUserInfoResponse> response = googleUserInfoFeignClient.call("Bearer " + accessToken);

        GoogleUserInfoResponse body = response.getBody();

        if (body == null) {
            throw new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE);
        }

        return body.getEmail();
    }
}
