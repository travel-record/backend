package world.trecord.web.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.security.JwtProvider;
import world.trecord.web.security.JwtResolver;
import world.trecord.web.service.auth.google.GoogleAuthManager;
import world.trecord.web.service.auth.response.LoginResponse;
import world.trecord.web.service.auth.response.RefreshResponse;
import world.trecord.web.service.users.UserService;

@RequiredArgsConstructor
@Component
public class AuthHandler {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final JwtResolver jwtResolver;
    private final GoogleAuthManager googleAuthManager;

    public LoginResponse googleLogin(String authorizationCode, String redirectionUri) {
        String email = googleAuthManager.getUserEmail(authorizationCode, redirectionUri);

        UserEntity userEntity = findOrCreateUserBy(email);

        return LoginResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .token(jwtProvider.createTokenWith(userEntity.getId()))
                .refreshToken(jwtProvider.createRefreshTokenWith(userEntity.getId()))
                .build();
    }


    public RefreshResponse reissueTokenWith(String refreshToken) {
        jwtResolver.validate(refreshToken);

        Long userId = Long.valueOf(jwtResolver.extractUserIdFrom(refreshToken));

        return RefreshResponse.builder()
                .token(jwtProvider.createTokenWith(userId))
                .refreshToken(jwtProvider.createRefreshTokenWith(userId))
                .build();
    }

    private UserEntity findOrCreateUserBy(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            userEntity = userService.createNewUserWith(email);
        }

        return userEntity;
    }
}
