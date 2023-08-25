package world.trecord.domain.record;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "record")
@Entity
public class RecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_record", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "place", nullable = false)
    private String place;

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

    @Column(name = "companion", nullable = true)
    private String companion;

    @Column(name = "image_url", nullable = true, columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feed", nullable = false, foreignKey = @ForeignKey(name = "fk_record_feed"))
    private FeedEntity feedEntity;

    @OneToMany(mappedBy = "recordEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> commentEntities;

    @Builder
    private RecordEntity(FeedEntity feedEntity, String title, LocalDateTime date, String place, String feeling, String weather, String transportation, String content, String companion, String imageUrl) {
        if (feedEntity != null) {
            this.feedEntity = feedEntity;
            feedEntity.addRecordEntity(this);
        }
        this.title = title;
        this.date = date;
        this.place = place;
        this.feeling = feeling;
        this.weather = weather;
        this.transportation = transportation;
        this.content = content;
        this.companion = companion;
        this.imageUrl = imageUrl;
        this.commentEntities = new ArrayList<>();
    }

    public void update(RecordEntity updateEntity) {
        this.title = updateEntity.getTitle();
        this.date = updateEntity.getDate();
        this.place = updateEntity.getPlace();
        this.feeling = updateEntity.getFeeling();
        this.weather = updateEntity.getWeather();
        this.transportation = updateEntity.getTransportation();
        this.content = updateEntity.getContent();
        this.companion = updateEntity.getCompanion();
        this.imageUrl = updateEntity.getImageUrl();
    }

    public void addCommentEntity(CommentEntity commentEntity) {
        this.commentEntities.add(commentEntity);
    }

    public LocalDate convertDateToLocalDate() {
        return this.date != null ? getDate().toLocalDate() : null;
    }
}
