package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.projection.FeedWithRecordProjection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class FeedInfoResponse {
    private Long writerId;
    private Long feedId;
    private Boolean isUpdatable;
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
    private FeedInfoResponse(List<FeedWithRecordProjection> projectionList, Long viewerId) {
        FeedWithRecordProjection feedEntity = projectionList.get(0);

        this.writerId = feedEntity.getWriterId();
        this.feedId = feedEntity.getFeedId();
        this.name = feedEntity.getFeedName();
        this.isUpdatable = writerId.equals(viewerId);
        this.imageUrl = feedEntity.getFeedImageUrl();
        this.description = feedEntity.getFeedDescription();
        this.satisfaction = feedEntity.getFeedSatisfaction();
        this.place = feedEntity.getFeedPlace();
        this.companion = feedEntity.getFeedCompanion();
        this.startAt = feedEntity.getFeedStartAt();
        this.endAt = feedEntity.getFeedEndAt();
        this.records = feedEntity.getRecordId() != null ?
                projectionList.stream().map(Record::new).toList() : new ArrayList<>();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Record {
        private Long id;
        private String title;
        private String place;
        private LocalDate date;

        public Record(FeedWithRecordProjection projection) {
            this.id = projection.getRecordId();
            this.title = projection.getRecordTitle();
            this.place = projection.getRecordPlace();
            this.date = projection.getRecordDate();
        }
    }
}
