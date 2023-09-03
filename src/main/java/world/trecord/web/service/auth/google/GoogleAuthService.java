package world.trecord.web.service.auth.google;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import world.trecord.web.client.feign.client.GoogleTokenFeignClient;
import world.trecord.web.client.feign.client.GoogleUserInfoFeignClient;
import world.trecord.web.client.feign.client.request.GoogleTokenRequest;
import world.trecord.web.client.feign.client.response.GoogleTokenResponse;
import world.trecord.web.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.web.exception.CustomException;
import world.trecord.web.properties.GoogleProperties;

import java.util.Objects;
import java.util.Optional;

import static world.trecord.web.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@RequiredArgsConstructor
@Service
public class GoogleAuthService {

    private static final String BEARER = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";

    private final GoogleProperties googleProperties;
    private final GoogleTokenFeignClient googleTokenFeignClient;
    private final GoogleUserInfoFeignClient googleUserInfoFeignClient;

    public String getUserEmail(String authorizationCode, String redirectionUri) {
        String token = requestTokenOrException(authorizationCode, redirectionUri);
        return fetchEmailOrException(token);
    }

    private String requestTokenOrException(String authorizationCode, String redirectionUri) {
        GoogleTokenRequest request = doBuildTokenRequest(authorizationCode, redirectionUri);

        return Optional.ofNullable(googleTokenFeignClient.requestToken(request))
                .filter(Objects::nonNull)
                .map(GoogleTokenResponse::getAccessToken)
                .filter(Objects::nonNull)
                .orElseThrow(() -> new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE));
    }

    private String fetchEmailOrException(String accessToken) {
        return Optional.ofNullable(googleUserInfoFeignClient.fetchUserInfo(BEARER + accessToken))
                .filter(Objects::nonNull)
                .map(GoogleUserInfoResponse::getEmail)
                .filter(Objects::nonNull)
                .orElseThrow(() -> new CustomException(INVALID_GOOGLE_AUTHORIZATION_CODE));
    }

    private GoogleTokenRequest doBuildTokenRequest(String authorizationCode, String redirectionUri) {
        return GoogleTokenRequest.builder()
                .client_id(googleProperties.getClientId())
                .client_secret(googleProperties.getClientSecret())
                .code(authorizationCode)
                .redirect_uri(redirectionUri)
                .grant_type(GRANT_TYPE)
                .build();
    }
}
