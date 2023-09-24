package world.trecord.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;
import static world.trecord.domain.users.Role.ROLE_USER;

@Data
@NoArgsConstructor(access = PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnonymousContext implements UserDetails, AccountContext {

    private Long id = null;

    public static AnonymousContext of() {
        return new AnonymousContext();
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(ROLE_USER.name());
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return UUID.randomUUID().toString();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return "anonymous";
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return false;
    }
}
