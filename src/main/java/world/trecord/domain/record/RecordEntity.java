package world.trecord.domain.record;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "record")
@SQLDelete(sql = "UPDATE record SET deleted_date_time = NOW() WHERE id_record = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class RecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_record", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "place", nullable = false)
    private String place;

    @Column(name = "latitude", nullable = false)
    private String latitude;

    @Column(name = "longitude", nullable = false)
    private String longitude;

    @Column(name = "feeling", nullable = false)
    private String feeling;

    @Column(name = "weather", nullable = false)
    private String weather;

    @Column(name = "transportation", nullable = false)
    private String transportation;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    // TODO delete
    @Column(name = "companion")
    private String companion;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feed", nullable = false, foreignKey = @ForeignKey(name = "fk_record_feed"))
    private FeedEntity feedEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_author", nullable = false, foreignKey = @ForeignKey(name = "fk_record_users"))
    private UserEntity userEntity;

    @Builder
    private RecordEntity(UserEntity userEntity,
                         FeedEntity feedEntity,
                         String title,
                         LocalDateTime date,
                         String place,
                         String feeling,
                         String longitude,
                         String latitude,
                         String weather,
                         String transportation,
                         String content,
                         String companion,
                         String imageUrl,
                         int sequence) {
        this.userEntity = userEntity;
        this.feedEntity = feedEntity;
        this.title = title;
        this.date = date;
        this.place = place;
        this.longitude = longitude;
        this.latitude = latitude;
        this.feeling = feeling;
        this.weather = weather;
        this.transportation = transportation;
        this.content = content;
        this.companion = companion;
        this.imageUrl = imageUrl;
        this.sequence = sequence;
    }

    public void update(RecordEntity updateEntity) {
        this.title = updateEntity.getTitle();
        this.date = updateEntity.getDate();
        this.place = updateEntity.getPlace();
        this.longitude = updateEntity.getLongitude();
        this.latitude = updateEntity.getLatitude();
        this.feeling = updateEntity.getFeeling();
        this.weather = updateEntity.getWeather();
        this.transportation = updateEntity.getTransportation();
        this.content = updateEntity.getContent();
        this.companion = updateEntity.getCompanion();
        this.imageUrl = updateEntity.getImageUrl();
    }

    public LocalDate convertDateToLocalDate() {
        return Objects.nonNull(this.date) ? this.date.toLocalDate() : null;
    }

    public boolean hasSameFeed(RecordEntity otherRecord) {
        return this.feedEntity.isEqualTo(otherRecord.getFeedEntity());
    }

    public boolean isCreatedBy(Long userId) {
        return Objects.equals(this.userEntity.getId(), userId);
    }

    public void swapSequenceWith(RecordEntity otherRecord) {
        int tmpSequence = otherRecord.getSequence();
        otherRecord.sequence = this.sequence;
        this.sequence = tmpSequence;
    }

    public Long getFeedId() {
        return Objects.nonNull(this.feedEntity) ? this.feedEntity.getId() : null;
    }

    public Long getUserId() {
        return Objects.nonNull(this.userEntity) ? this.userEntity.getId() : null;
    }

    public boolean isUpdatable(Long userId) {
        return isCreatedBy(userId) || this.feedEntity.isOwnedBy(userId);
    }
}
