package world.trecord.web.feign.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class GoogleTokenResponse {
    private String access_token;
}
