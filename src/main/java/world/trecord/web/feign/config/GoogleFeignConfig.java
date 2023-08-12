package world.trecord.web.feign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import world.trecord.web.feign.decoder.GoogleFeignErrorDecoder;

@Configuration
public class GoogleFeignConfig {

    @Bean
    public GoogleFeignErrorDecoder googleErrorDecoder() {
        return new GoogleFeignErrorDecoder();
    }
}
