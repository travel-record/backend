package world.trecord.domain.feed;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "feed",
        indexes = {
                @Index(name = "idx_feed_users", columnList = "id_owner")
        }
)
@SQLDelete(sql = "UPDATE feed SET deleted_date_time = NOW() WHERE id_feed = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class FeedEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feed", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "companion")
    private String companion;

    @Column(name = "place")
    private String place;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "satisfaction")
    private String satisfaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_owner", nullable = false, foreignKey = @ForeignKey(name = "fk_feed_users"))
    private UserEntity userEntity;

    @OneToMany(mappedBy = "feedEntity")
    private Set<FeedContributorEntity> feedContributors = new HashSet<>();

    @Builder
    private FeedEntity(UserEntity userEntity,
                       String name,
                       String description,
                       String imageUrl,
                       LocalDateTime startAt,
                       LocalDateTime endAt,
                       String companion,
                       String place,
                       String longitude,
                       String latitude,
                       String satisfaction) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.companion = companion;
        this.place = place;
        this.longitude = longitude;
        this.latitude = latitude;
        this.satisfaction = satisfaction;
        this.userEntity = userEntity;
    }

    public void update(FeedEntity updateEntity) {
        this.name = updateEntity.getName();
        this.imageUrl = updateEntity.getImageUrl();
        this.description = updateEntity.getDescription();
        this.startAt = updateEntity.getStartAt();
        this.endAt = updateEntity.getEndAt();
        this.companion = updateEntity.getCompanion();
        this.longitude = updateEntity.getLongitude();
        this.latitude = updateEntity.getLatitude();
        this.place = updateEntity.getPlace();
        this.satisfaction = updateEntity.getSatisfaction();
    }

    public boolean isEqualTo(FeedEntity otherFeed) {
        return Objects.equals(this.id, otherFeed.getId());
    }

    public LocalDate convertStartAtToLocalDate() {
        return Objects.nonNull(this.startAt) ? this.startAt.toLocalDate() : null;
    }

    public LocalDate convertEndAtToLocalDate() {
        return Objects.nonNull(this.endAt) ? this.endAt.toLocalDate() : null;
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(this.userEntity.getId(), userId);
    }

    public boolean canWriteRecord(Long userId) {
        if (isOwnedBy(userId)) {
            return true;
        }
        return feedContributors.stream()
                .map(FeedContributorEntity::getUserEntity)
                .map(UserEntity::getId)
                .anyMatch(contributorUserId -> Objects.equals(contributorUserId, userId));
    }

    public void addFeedContributor(FeedContributorEntity feedContributorEntity) {
        this.feedContributors.add(feedContributorEntity);
    }

    public boolean isContributor(Long userId) {
        return feedContributors.stream().anyMatch(contributor -> Objects.equals(contributor.getUserEntity().getId(), userId));
    }

    public void removeFeedContributor(Long userId) {
        feedContributors.removeIf(contributor -> Objects.equals(contributor.getUserEntity().getId(), userId));
    }

    public Long getUserId() {
        return Objects.nonNull(this.userEntity) ? this.userEntity.getId() : null;
    }

    public String getUserNickname() {
        return Objects.nonNull(this.userEntity) ? this.userEntity.getNickname() : null;
    }

    public List<FeedContributorEntity> getContributors() {
        return Objects.nonNull(this.feedContributors) ? this.feedContributors.stream().toList() : null;
    }
}
