package world.trecord.web.client.feign.client.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class GoogleTokenResponse {
    private String access_token;
}
