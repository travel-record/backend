package world.trecord.web.client.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import world.trecord.web.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.web.client.feign.config.GoogleFeignConfig;

@FeignClient(
        name = "google-user-info-client",
        url = "https://www.googleapis.com/oauth2/v3/userinfo",
        configuration = GoogleFeignConfig.class
)
public interface GoogleUserInfoFeignClient {

    @GetMapping
    ResponseEntity<GoogleUserInfoResponse> call(@RequestHeader("Authorization") String accessToken);
}
