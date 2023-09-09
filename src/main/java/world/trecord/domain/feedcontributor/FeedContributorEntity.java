package world.trecord.domain.feedcontributor;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feedcontributor.args.FeedContributorPermissionArgs;
import world.trecord.domain.users.UserEntity;

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

    @Type(JsonType.class)
    @Column(name = "permission", columnDefinition = "json")
    private FeedContributorPermissionArgs permission;

    @Builder
    private FeedContributorEntity(UserEntity userEntity, FeedEntity feedEntity) {
        this.userEntity = userEntity;
        this.feedEntity = feedEntity;
        this.permission = new FeedContributorPermissionArgs();
    }
}
