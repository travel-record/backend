package world.trecord.web.client.feign.config;

import feign.Logger;
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
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
