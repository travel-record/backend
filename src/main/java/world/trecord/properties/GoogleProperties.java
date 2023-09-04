package world.trecord.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "google")
@Validated
public class GoogleProperties {

    @NotBlank(message = "Google Client ID must not be blank")
    private String clientId;

    @NotBlank(message = "Google Client Secret must not be blank")
    private String clientSecret;

    private final Oauth2 oauth2 = new Oauth2();
    private final Api api = new Api();

    @Setter
    @Getter
    public static class Oauth2 {
        @NotBlank(message = "OAuth2 endpoint must not be blank")
        private String endpoint;
    }

    @Setter
    @Getter
    public static class Api {
        @NotBlank(message = "API base URL must not be blank")
        private String baseUrl;
    }
}
