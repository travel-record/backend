package world.trecord.web.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;
import world.trecord.web.service.auth.google.GoogleAuthService;
import world.trecord.web.service.auth.response.LoginResponse;
import world.trecord.web.service.auth.response.RefreshResponse;
import world.trecord.web.service.users.UserService;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenHandler jwtTokenHandler;
    private final GoogleAuthService googleAuthService;
    private final JwtProperties jwtProperties;

    public LoginResponse googleLogin(String authorizationCode, String redirectionUri) {
        String email = googleAuthService.getUserEmail(authorizationCode, redirectionUri);

        UserEntity userEntity = getOrCreateUser(email);

        String secretKey = jwtProperties.getSecretKey();
        Long expiredTimeMs = jwtProperties.getTokenExpiredTimeMs();

        return LoginResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .token(jwtTokenHandler.generateToken(userEntity.getId(), secretKey, expiredTimeMs))
                .refreshToken(jwtTokenHandler.generateToken(userEntity.getId(), secretKey, expiredTimeMs * 14))
                .build();
    }

    public RefreshResponse reissueToken(String refreshToken) {
        String secretKey = jwtProperties.getSecretKey();
        Long expiredTimeMs = jwtProperties.getTokenExpiredTimeMs();

        jwtTokenHandler.verify(secretKey, refreshToken);

        Long userId = jwtTokenHandler.getUserId(secretKey, refreshToken);

        return RefreshResponse.builder()
                .token(jwtTokenHandler.generateToken(userId, secretKey, expiredTimeMs))
                .refreshToken(jwtTokenHandler.generateToken(userId, secretKey, expiredTimeMs * 14))
                .build();
    }

    private UserEntity getOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userService.createNewUser(email));
    }
}
