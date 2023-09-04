package world.trecord.client.feign.config;

import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import world.trecord.client.feign.decoder.GoogleFeignErrorDecoder;
import world.trecord.properties.GoogleProperties;

@RequiredArgsConstructor
@Configuration
public class GoogleFeignConfig {

    private final GoogleProperties googleProperties;

    @Bean
    public GoogleFeignErrorDecoder googleErrorDecoder() {
        return new GoogleFeignErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean(name = "googleOauth2Endpoint")
    public String googleOauth2Endpoint() {
        return googleProperties.getOauth2().getEndpoint();
    }

    @Bean(name = "googleApiBaseUrl")
    public String googleApiBaseUrl() {
        return googleProperties.getApi().getBaseUrl();
    }
}
