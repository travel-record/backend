package world.trecord.client.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import world.trecord.client.feign.client.response.GoogleUserInfoResponse;
import world.trecord.client.feign.config.GoogleFeignConfig;

@FeignClient(
        name = "google-user-info-client",
        url = "#{@googleApiBaseUrl}",
        configuration = GoogleFeignConfig.class
)
public interface GoogleUserInfoFeignClient {

    @GetMapping("/oauth2/v3/userinfo")
    GoogleUserInfoResponse fetchUserInfo(@RequestHeader("Authorization") String accessToken);
}
