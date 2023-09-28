package world.trecord.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.redis.UserCacheRepository;
import world.trecord.config.security.JwtTokenHandler;
import world.trecord.config.security.account.UserContext;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.auth.response.LoginResponse;
import world.trecord.dto.auth.response.RefreshResponse;
import world.trecord.service.users.UserService;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final Long REFRESH_TOKEN_MULTIPLIER = 14L;
    private final UserService userService;
    private final UserCacheRepository userCacheRepository;
    private final JwtTokenHandler jwtTokenHandler;
    private final GoogleAuthHandler googleAuthHandler;
    private final JwtProperties jwtProperties;

    public LoginResponse googleLogin(String authorizationCode, String redirectionUri) {
        String email = googleAuthHandler.getUserEmail(authorizationCode, redirectionUri);
        UserEntity userEntity = userService.findOrCreateUser(email);
        String issuedToken = createToken(userEntity.getId());
        String issuedRefreshToken = createRefreshToken(userEntity.getId());
        userCacheRepository.setUserContext(UserContext.fromEntity(userEntity));
        return buildLoginResponse(userEntity, issuedToken, issuedRefreshToken);
    }

    public RefreshResponse reissueToken(String refreshToken) {
        String secretKey = jwtProperties.getSecretKey();
        jwtTokenHandler.verifyToken(secretKey, refreshToken);
        Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, refreshToken);
        UserEntity userEntity = userService.findUserOrException(userId);
        String reissuedToken = createToken(userEntity.getId());
        String reissuedRefreshToken = createRefreshToken(userEntity.getId());
        return buildRefreshResponse(reissuedToken, reissuedRefreshToken);
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }

    private String createRefreshToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs() * REFRESH_TOKEN_MULTIPLIER);
    }

    private LoginResponse buildLoginResponse(UserEntity userEntity, String issuedToken, String issuedRefreshToken) {
        return LoginResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .token(issuedToken)
                .refreshToken(issuedRefreshToken)
                .build();
    }

    private RefreshResponse buildRefreshResponse(String reissuedToken, String reissuedRefreshToken) {
        return RefreshResponse.builder()
                .token(reissuedToken)
                .refreshToken(reissuedRefreshToken)
                .build();
    }
}
