package world.trecord.web.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.controller.auth.request.GoogleLoginRequest;
import world.trecord.web.controller.auth.request.RefreshTokenRequest;
import world.trecord.web.service.auth.AuthHandler;
import world.trecord.web.service.auth.response.LoginResponse;
import world.trecord.web.service.auth.response.RefreshResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/auth")
public class AuthController {

    private final AuthHandler authHandler;

    @PostMapping("/google-login")
    public ApiResponse<LoginResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        return ApiResponse.ok(authHandler.googleLogin(request.getAuthorizationCode(), request.getRedirectionUri()));
    }

    @PostMapping("/token")
    public ApiResponse<RefreshResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.ok(authHandler.reissueTokenWith(request.getRefreshToken()));
    }

}
