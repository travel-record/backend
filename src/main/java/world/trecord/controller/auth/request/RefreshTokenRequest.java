package world.trecord.controller.auth.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RefreshTokenRequest {

    @NotEmpty
    private String refreshToken;

    @Builder
    private RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
