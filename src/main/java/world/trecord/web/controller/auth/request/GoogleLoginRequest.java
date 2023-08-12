package world.trecord.web.controller.auth.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class GoogleLoginRequest {

    @NotEmpty
    private String authorizationCode;

    @Builder
    private GoogleLoginRequest(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}
