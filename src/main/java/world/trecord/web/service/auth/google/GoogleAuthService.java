package world.trecord.web.service.auth.google;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import world.trecord.web.client.feign.client.GoogleTokenFeignClient;
import world.trecord.web.client.feign.client.GoogleUserInfoFeignClient;
import world.trecord.web.client.feign.client.request.GoogleTokenRequest;
import world.trecord.web.client.feign.client.response.GoogleTokenResponse;
import world.trecord.web.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.web.exception.CustomException;

import java.util.Optional;

import static world.trecord.web.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@RequiredArgsConstructor
@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    private static final String BEARER = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";

    private final GoogleTokenFeignClient googleTokenFeignClient;
    private final GoogleUserInfoFeignClient googleUserInfoFeignClient;

    public String getUserEmail(String authorizationCode, String redirectionUri) {
        String token = getToken(authorizationCode, redirectionUri);
        return getEmail(token);
    }

    private String getToken(String authorizationCode, String redirectionUri) throws CustomException {
        GoogleTokenRequest request = GoogleTokenRequest.builder()
                .client_id(googleClientId)
                .client_secret(googleClientSecret)
                .code(authorizationCode)
                .redirect_uri(redirectionUri)
                .grant_type(GRANT_TYPE)
                .build();

        return Optional.ofNullable(googleTokenFeignClient.requestToken(request))
                .map(GoogleTokenResponse::getAccessToken)
                .orElseThrow(() -> new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE));
    }

    private String getEmail(String accessToken) {
        return Optional.ofNullable(googleUserInfoFeignClient.fetchUserInfo(BEARER + accessToken))
                .map(GoogleUserInfoResponse::getEmail)
                .orElseThrow(() -> new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE));
    }
}
