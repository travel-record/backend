package world.trecord.web.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.security.jwt.JwtGenerator;
import world.trecord.web.security.jwt.JwtParser;
import world.trecord.web.service.auth.google.GoogleAuthManager;
import world.trecord.web.service.auth.response.LoginResponse;
import world.trecord.web.service.auth.response.RefreshResponse;
import world.trecord.web.service.users.UserService;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AuthHandler {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtGenerator jwtGenerator;
    private final JwtParser jwtParser;
    private final GoogleAuthManager googleAuthManager;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    public LoginResponse googleLogin(String authorizationCode, String redirectionUri) {
        String email = googleAuthManager.getUserEmail(authorizationCode, redirectionUri);

        UserEntity userEntity = getOrCreateUserBy(email);

        return LoginResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .token(jwtGenerator.generateToken(userEntity.getId(), secretKey, expiredTimeMs))
                .refreshToken(jwtGenerator.generateToken(userEntity.getId(), secretKey, expiredTimeMs * 14))
                .build();
    }

    public RefreshResponse reissueTokenWith(String refreshToken) {
        jwtParser.verify(secretKey, refreshToken);

        Long userId = Long.valueOf(jwtParser.extractUserId(secretKey, refreshToken));

        return RefreshResponse.builder()
                .token(jwtGenerator.generateToken(userId, secretKey, expiredTimeMs))
                .refreshToken(jwtGenerator.generateToken(userId, secretKey, expiredTimeMs * 14))
                .build();
    }

    private UserEntity getOrCreateUserBy(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email))
                .orElseGet(() -> userService.createNewUserWith(email));
    }
}
