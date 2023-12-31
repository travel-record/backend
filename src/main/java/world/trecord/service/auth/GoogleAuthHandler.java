package world.trecord.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import world.trecord.client.feign.client.GoogleTokenFeignClient;
import world.trecord.client.feign.client.GoogleUserInfoFeignClient;
import world.trecord.client.feign.client.request.GoogleTokenRequest;
import world.trecord.client.feign.client.response.GoogleTokenResponse;
import world.trecord.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.config.properties.GoogleProperties;
import world.trecord.exception.CustomException;

import java.util.Objects;
import java.util.Optional;

import static world.trecord.exception.CustomExceptionError.INVALID_GOOGLE_AUTHORIZATION_CODE;

@RequiredArgsConstructor
@Service
public class GoogleAuthHandler {

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
        GoogleTokenRequest request = buildTokenRequest(authorizationCode, redirectionUri);
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

    private GoogleTokenRequest buildTokenRequest(String authorizationCode, String redirectionUri) {
        return GoogleTokenRequest.builder()
                .clientId(googleProperties.getClientId())
                .clientSecret(googleProperties.getClientSecret())
                .code(authorizationCode)
                .redirectUri(redirectionUri)
                .grantType(GRANT_TYPE)
                .build();
    }
}
