package world.trecord.web.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import world.trecord.domain.users.UserEntity;

import java.util.List;

@Getter
public class UserContext extends User {
    private final UserEntity userEntity;

    public UserContext(UserEntity userEntity, List<GrantedAuthority> grantedAuthorities) {
        super(String.valueOf(userEntity.getId()), "", grantedAuthorities);
        this.userEntity = userEntity;
    }
}
