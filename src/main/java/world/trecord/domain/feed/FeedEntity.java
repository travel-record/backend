package world.trecord.domain.feed;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "feed")
@SQLDelete(sql = "UPDATE feed SET deleted_date_time = NOW() WHERE id_feed = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class FeedEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feed", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url", nullable = true, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "companion", nullable = true)
    private String companion;

    @Column(name = "place", nullable = true)
    private String place;

    @Column(name = "satisfaction", nullable = true)
    private String satisfaction;

    @Column(name = "deleted_date_time", nullable = true)
    private LocalDateTime deletedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", nullable = false, foreignKey = @ForeignKey(name = "fk_feed_users"))
    private UserEntity userEntity;

    @OneToMany(mappedBy = "feedEntity", cascade = CascadeType.ALL)
    private List<RecordEntity> recordEntities = new ArrayList<>();

    @Builder
    private FeedEntity(UserEntity userEntity, String name, String description, String imageUrl, LocalDateTime startAt, LocalDateTime endAt, String companion, String place, String satisfaction) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.companion = companion;
        this.place = place;
        this.satisfaction = satisfaction;
        this.userEntity = userEntity;
        this.deletedDateTime = null;
    }

    public void addRecordEntity(RecordEntity recordEntity) {
        recordEntities.add(recordEntity);
    }

    public void update(FeedEntity updateEntity) {
        this.name = updateEntity.getName();
        this.imageUrl = updateEntity.getImageUrl();
        this.description = updateEntity.getDescription();
        this.startAt = updateEntity.getStartAt();
        this.endAt = updateEntity.getEndAt();
        this.companion = updateEntity.getCompanion();
        this.place = updateEntity.getPlace();
        this.satisfaction = updateEntity.getSatisfaction();
    }

    public Stream<RecordEntity> sortRecordEntitiesByDateAndCreatedTimeAsc() {
        return this.recordEntities.stream()
                .sorted(Comparator.comparing(RecordEntity::getDate)
                        .thenComparing(RecordEntity::getCreatedDateTime));
    }

    public boolean isEqualTo(FeedEntity otherFeed) {
        return Objects.equals(this.id, otherFeed.getId());
    }

    public LocalDate convertStartAtToLocalDate() {
        return this.startAt != null ? this.startAt.toLocalDate() : null;
    }

    public LocalDate convertEndAtToLocalDate() {
        return this.endAt != null ? this.endAt.toLocalDate() : null;
    }

    public boolean isManagedBy(Long userId) {
        return Objects.equals(this.userEntity.getId(), userId);
    }
}
