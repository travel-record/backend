package world.trecord.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.trecord.controller.ApiResponse;
import world.trecord.controller.auth.request.GoogleLoginRequest;
import world.trecord.controller.auth.request.RefreshTokenRequest;
import world.trecord.service.auth.AuthService;
import world.trecord.service.auth.response.LoginResponse;
import world.trecord.service.auth.response.RefreshResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google-login")
    public ApiResponse<LoginResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        return ApiResponse.ok(authService.googleLogin(request.getAuthorizationCode(), request.getRedirectionUri()));
    }

    @PostMapping("/token")
    public ApiResponse<RefreshResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.ok(authService.reissueToken(request.getRefreshToken()));
    }
}
