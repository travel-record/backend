package world.trecord.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.redis.UserCacheRepository;
import world.trecord.config.security.JwtTokenHandler;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.auth.response.LoginResponse;
import world.trecord.dto.auth.response.RefreshResponse;
import world.trecord.dto.users.UserContext;
import world.trecord.exception.CustomException;
import world.trecord.service.users.UserService;

import static world.trecord.exception.CustomExceptionError.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final Long REFRESH_TOKEN_MULTIPLIER = 14L;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenHandler jwtTokenHandler;
    private final GoogleAuthService googleAuthService;
    private final UserCacheRepository userCacheRepository;
    private final JwtProperties jwtProperties;

    public LoginResponse googleLogin(String authorizationCode, String redirectionUri) {
        String email = googleAuthService.getUserEmail(authorizationCode, redirectionUri);
        UserEntity userEntity = findOrCreateUser(email);
        String issuedToken = createToken(userEntity.getId());
        String issuedRefreshToken = createRefreshToken(userEntity.getId());

        userCacheRepository.setUserContext(UserContext.fromEntity(userEntity));

        return LoginResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .token(issuedToken)
                .refreshToken(issuedRefreshToken)
                .build();
    }

    public RefreshResponse reissueToken(String refreshToken) {
        String secretKey = jwtProperties.getSecretKey();
        jwtTokenHandler.verifyToken(secretKey, refreshToken);
        Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, refreshToken);
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String reissuedToken = createToken(userEntity.getId());
        String reissuedRefreshToken = createRefreshToken(userEntity.getId());

        return RefreshResponse.builder()
                .token(reissuedToken)
                .refreshToken(reissuedRefreshToken)
                .build();
    }

    private UserEntity findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    try {
                        return userService.createUser(email);
                    } catch (DataIntegrityViolationException ex) {
                        return userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalStateException("Unexpected error while retrieving the user with email: " + email, ex));
                    }
                });
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }

    private String createRefreshToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs() * REFRESH_TOKEN_MULTIPLIER);
    }
}
