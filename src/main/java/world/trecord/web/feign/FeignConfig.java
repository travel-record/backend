package world.trecord.web.feign;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import world.trecord.web.feign.logger.GoogleClientLogger;

@EnableFeignClients
@Configuration
public class FeignConfig {
    @Bean
    public Logger googleClientLogger() {
        return new GoogleClientLogger();
    }
}
