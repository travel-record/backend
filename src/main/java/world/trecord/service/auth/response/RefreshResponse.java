package world.trecord.service.auth.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshResponse {
    private String token;
    private String refreshToken;

    @Builder
    public RefreshResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
