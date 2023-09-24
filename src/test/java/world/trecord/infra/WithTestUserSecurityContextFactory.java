package world.trecord.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.users.UserContext;
import world.trecord.service.users.UserService;

@RequiredArgsConstructor
public class WithTestUserSecurityContextFactory implements WithSecurityContextFactory<WithTestUser> {

    private final UserService userService;

    @Override
    public SecurityContext createSecurityContext(WithTestUser withTestUser) {
        String email = withTestUser.value();
        UserEntity userEntity = userService.createUser(email);
        UserContext userContext = userService.getUserContextOrException(userEntity.getId());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userContext, userContext.getPassword(), userContext.getAuthorities());
        securityContext.setAuthentication(authentication);
        return securityContext;
    }
}
