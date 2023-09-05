package world.trecord.service.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContext implements UserDetails {

    private Long id;
    private String nickname;
    private String introduction;
    private String imageUrl;
    private String role;
    @JsonIgnore
    private LocalDateTime createdDateTime;
    @JsonIgnore
    private LocalDateTime modifiedDateTime;
    @JsonIgnore
    private LocalDateTime deletedDateTime;

    public static UserContext fromEntity(UserEntity userEntity) {
        return new UserContext(
                userEntity.getId(),
                userEntity.getNickname(),
                userEntity.getIntroduction(),
                userEntity.getImageUrl(),
                userEntity.getRole(),
                userEntity.getCreatedDateTime(),
                userEntity.getModifiedDateTime(),
                userEntity.getDeletedDateTime()
        );
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(this.role);
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return UUID.randomUUID().toString();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return String.valueOf(this.id);
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return deletedDateTime == null;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return deletedDateTime == null;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return deletedDateTime == null;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return deletedDateTime == null;
    }
}
