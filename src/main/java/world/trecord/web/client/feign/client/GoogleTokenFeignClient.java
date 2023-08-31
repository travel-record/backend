package world.trecord.web.client.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import world.trecord.web.client.feign.client.request.GoogleTokenRequest;
import world.trecord.web.client.feign.client.response.GoogleTokenResponse;
import world.trecord.web.client.feign.config.GoogleFeignConfig;

@FeignClient(
        name = "google-token-client",
        url = "#{@googleOauth2Endpoint}",
        configuration = GoogleFeignConfig.class
)
public interface GoogleTokenFeignClient {

    @PostMapping("/token")
    GoogleTokenResponse requestToken(@RequestBody GoogleTokenRequest request);
}
