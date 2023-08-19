package world.trecord.web.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import world.trecord.web.feign.client.request.GoogleTokenRequest;
import world.trecord.web.feign.client.response.GoogleTokenResponse;
import world.trecord.web.feign.config.GoogleFeignConfig;

@FeignClient(
        name = "google-token-client",
        url = "https://oauth2.googleapis.com/token",
        configuration = GoogleFeignConfig.class
)
public interface GoogleTokenFeignClient {

    @PostMapping
    ResponseEntity<GoogleTokenResponse> call(@RequestBody GoogleTokenRequest request);
}
