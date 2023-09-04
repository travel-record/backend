package world.trecord.client.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import world.trecord.client.feign.client.request.GoogleTokenRequest;
import world.trecord.client.feign.config.GoogleFeignConfig;
import world.trecord.client.feign.client.response.GoogleTokenResponse;

@FeignClient(
        name = "google-token-client",
        url = "#{@googleOauth2Endpoint}",
        configuration = GoogleFeignConfig.class
)
public interface GoogleTokenFeignClient {

    @PostMapping("/token")
    GoogleTokenResponse requestToken(@RequestBody GoogleTokenRequest request);
}
