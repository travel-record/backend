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

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "record_sequence",
        indexes = {
                @Index(name = "idx_feed_date", columnList = "id_feed, date", unique = true)
        }
)
@SQLDelete(sql = "UPDATE record_sequence SET deleted_date_time = NOW() WHERE id_sequence = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class RecordSequenceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sequence", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feed", nullable = false, foreignKey = @ForeignKey(name = "fk_sequence_feed"))
    private FeedEntity feedEntity;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Builder
    private RecordSequenceEntity(FeedEntity feedEntity, LocalDateTime date, int sequence) {
        this.feedEntity = feedEntity;
        this.date = date;
        this.sequence = sequence;
    }
}
