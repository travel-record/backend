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
        name = "feed_contributor",
        indexes = @Index(name = "idx_contributor_users_feed", columnList = "id_users, id_feed")
)
@SQLDelete(sql = "UPDATE feed_contributor SET deleted_date_time = NOW() WHERE id_contributor = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class FeedContributorEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contributor", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", foreignKey = @ForeignKey(name = "fk_contributor_users"))
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feed", foreignKey = @ForeignKey(name = "fk_contributor_feed"))
    private FeedEntity feedEntity;

    @ElementCollection(targetClass = FeedContributorPermission.class)
    @CollectionTable(name = "contributor_permission", joinColumns = @JoinColumn(name = "id_contributor"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Set<FeedContributorPermission> permissions;

    @Builder
    private FeedContributorEntity(UserEntity userEntity, FeedEntity feedEntity) {
        this.userEntity = userEntity;
        this.feedEntity = feedEntity;
        this.permissions = FeedContributorPermission.getAllPermissions();
    }
}
