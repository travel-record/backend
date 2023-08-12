package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class FeedOneResponse {

    private Long writerId;
    private Long feedId;
    private String name;
    private String imageUrl;
    private String description;
    private String companion;
    private String place;
    private String satisfaction;
    private LocalDate startAt;
    private LocalDate endAt;
    private List<Record> records;

    @Builder
    private FeedOneResponse(FeedEntity feedEntity) {
        this.writerId = feedEntity.getUserEntity().getId();
        this.feedId = feedEntity.getId();
        this.name = feedEntity.getName();
        this.imageUrl = feedEntity.getImageUrl();
        this.description = feedEntity.getDescription();
        this.satisfaction = feedEntity.getSatisfaction();
        this.place = feedEntity.getPlace();
        this.companion = feedEntity.getCompanion();
        this.startAt = feedEntity.convertStartAtToLocalDate();
        this.endAt = feedEntity.convertEndAtToLocalDate();
        this.records = feedEntity.sortRecordEntitiesByDateAndCreatedTimeAsc()
                .map(Record::new)
                .toList();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Record {
        private Long id;
        private String title;
        private String place;
        private LocalDate date;

        public Record(RecordEntity recordEntity) {
            this.id = recordEntity.getId();
            this.title = recordEntity.getTitle();
            this.place = recordEntity.getPlace();
            this.date = recordEntity.convertDateToLocalDate();
        }
    }
}
