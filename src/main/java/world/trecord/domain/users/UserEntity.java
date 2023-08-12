package world.trecord.domain.users;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.trecord.domain.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_users")
    private Long id;

    @Column(name = "email", nullable = false, length = 255, unique = true, updatable = false)
    private String email;

    @Column(name = "nickname", nullable = true, length = 20, unique = true)
    private String nickname;

    @Column(name = "image_url", nullable = true, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "introduction", nullable = true, length = 255)
    private String introduction;

    @Builder
    private UserEntity(String email, String nickname, String imageUrl, String introduction) {
        this.email = email;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
    }

    public void update(String nickname, String imageUrl, String introduction) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
    }
}
