package world.trecord.domain.users;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.web.security.Role;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_date_time = NOW() WHERE id_users = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_users", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "nickname", length = 20, unique = true)
    private String nickname;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "introduction")
    private String introduction;

    @Column(name = "deleted_date_time")
    private LocalDateTime deletedDateTime;

    @Builder
    private UserEntity(String email, String nickname, String imageUrl, String introduction) {
        this.email = email;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
    }

    public void update(UserEntity updateEntity) {
        this.nickname = updateEntity.getNickname();
        this.imageUrl = updateEntity.getImageUrl();
        this.introduction = updateEntity.getIntroduction();
    }

    public boolean isEqualTo(UserEntity otherEntity) {
        return Objects.equals(this.id, otherEntity.getId());
    }

    public String getRole() {
        return Role.ROLE_USER.name();
    }
}
