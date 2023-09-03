package world.trecord.client.feign.client.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class GoogleTokenRequest {
    private String client_id;
    private String client_secret;
    private String code;
    private String grant_type;
    private String redirect_uri;

    @Builder
    private GoogleTokenRequest(String client_id, String client_secret, String code, String grant_type, String redirect_uri) {
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.code = code;
        this.grant_type = grant_type;
        this.redirect_uri = redirect_uri;
    }
}
