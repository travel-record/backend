package world.trecord.service.auth;

import org.springframework.stereotype.Service;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.service.auth.google.GoogleAuthService;
import world.trecord.service.auth.response.RefreshResponse;
import world.trecord.service.users.UserService;
import world.trecord.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;
import world.trecord.service.auth.response.LoginResponse;

@Service
public class AuthService {

    private static final Long REFRESH_TOKEN_MULTIPLIER = 14L;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenHandler jwtTokenHandler;
    private final GoogleAuthService googleAuthService;
    private final String secretKey;
    private final Long tokenExpiredTimeMs;

    public AuthService(UserRepository userRepository, UserService userService, JwtTokenHandler jwtTokenHandler, GoogleAuthService googleAuthService, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtTokenHandler = jwtTokenHandler;
        this.googleAuthService = googleAuthService;
        this.secretKey = jwtProperties.getSecretKey();
        this.tokenExpiredTimeMs = jwtProperties.getTokenExpiredTimeMs();
    }

    public LoginResponse googleLogin(String authorizationCode, String redirectionUri) {
        String email = googleAuthService.getUserEmail(authorizationCode, redirectionUri);
        UserEntity userEntity = findOrCreateUser(email);
        String issuedToken = createToken(userEntity.getId());
        String issuedRefreshToken = createRefreshToken(userEntity.getId());

        return LoginResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .token(issuedToken)
                .refreshToken(issuedRefreshToken)
                .build();
    }

    public RefreshResponse reissueToken(String refreshToken) {
        jwtTokenHandler.verifyToken(secretKey, refreshToken);
        Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, refreshToken);
        // TODO userId 없으면 exception
        String reissuedToken = createToken(userId);
        String reissuedRefreshToken = createRefreshToken(userId);

        return RefreshResponse.builder()
                .token(reissuedToken)
                .refreshToken(reissuedRefreshToken)
                .build();
    }

    private UserEntity findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userService.createNewUser(email));
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, secretKey, tokenExpiredTimeMs);
    }

    private String createRefreshToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, secretKey, tokenExpiredTimeMs * REFRESH_TOKEN_MULTIPLIER);
    }
}
