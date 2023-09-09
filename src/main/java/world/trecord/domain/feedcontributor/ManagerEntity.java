package world.trecord.domain.feedcontributor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "manager",
        indexes = @Index(name = "idx_manager_users_feed", columnList = "id_users, id_feed")
)
@SQLDelete(sql = "UPDATE manager SET deleted_date_time = NOW() WHERE id_manager = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class ManagerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_manager", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", foreignKey = @ForeignKey(name = "fk_manager_users"))
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feed", foreignKey = @ForeignKey(name = "fk_manager_feed"))
    private FeedEntity feedEntity;

    @ElementCollection(targetClass = ManagerPermission.class)
    @CollectionTable(name = "manager_permission", joinColumns = @JoinColumn(name = "id_manager"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Set<ManagerPermission> permissions;

    @Builder
    private ManagerEntity(UserEntity userEntity, FeedEntity feedEntity) {
        this.userEntity = userEntity;
        this.feedEntity = feedEntity;
        this.permissions = ManagerPermission.getAllPermissions();
    }
}
