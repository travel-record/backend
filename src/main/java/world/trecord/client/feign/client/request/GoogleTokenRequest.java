package world.trecord.client.feign.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleTokenRequest {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    private String code;
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("redirect_uri")
    private String redirectUri;

    @Builder
    private GoogleTokenRequest(String clientId, String clientSecret, String code, String grantType, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.code = code;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
    }
}
