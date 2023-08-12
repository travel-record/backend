package world.trecord.web.service.auth.google;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import world.trecord.web.feign.client.GoogleTokenFeignClient;
import world.trecord.web.feign.client.GoogleUserInfoFeignClient;
import world.trecord.web.feign.request.GoogleTokenRequest;
import world.trecord.web.feign.response.GoogleTokenResponse;
import world.trecord.web.feign.response.GoogleUserInfoResponse;

@RequiredArgsConstructor
@Slf4j
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

    private String requestAccessTokenWith(String authorizationCode, String redirectionUri) {
        GoogleTokenRequest request = GoogleTokenRequest.builder()
                .client_id(googleClientId)
                .client_secret(googleClientSecret)
                .code(authorizationCode)
                .redirect_uri(redirectionUri)
                .grant_type("authorization_code")
                .build();

        ResponseEntity<GoogleTokenResponse> response = googleTokenFeignClient.call(request);
        return response.getBody().getAccess_token();
    }

    private String requestUserEmailWith(String accessToken) {
        ResponseEntity<GoogleUserInfoResponse> response = googleUserInfoFeignClient.call("Bearer " + accessToken);
        return response.getBody().getEmail();
    }
}
