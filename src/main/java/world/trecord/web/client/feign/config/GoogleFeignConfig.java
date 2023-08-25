package world.trecord.web.client.feign.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import world.trecord.web.client.feign.decoder.GoogleFeignErrorDecoder;

@Configuration
public class GoogleFeignConfig {

    @Bean
    public GoogleFeignErrorDecoder googleErrorDecoder() {
        return new GoogleFeignErrorDecoder();
    }

    @Bean
    public Retryer feignRetryer() {
        final int initialInterval = 1000;
        final int maxInterval = 1000;
        final int maxAttempts = 3;

        return new Retryer.Default(initialInterval, maxInterval, maxAttempts);
    }
}
