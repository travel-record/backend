package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.projection.RecordWithFeedProjection;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private FeedInfoResponse(FeedEntity feedEntity, Long viewerId, List<RecordWithFeedProjection> projectionList) {
        this.writerId = feedEntity.getUserEntity().getId();
        this.feedId = feedEntity.getId();
        this.isUpdatable = writerId.equals(viewerId);
        this.name = feedEntity.getName();
        this.imageUrl = feedEntity.getImageUrl();
        this.description = feedEntity.getDescription();
        this.satisfaction = feedEntity.getSatisfaction();
        this.place = feedEntity.getPlace();
        this.companion = feedEntity.getCompanion();
        this.startAt = feedEntity.convertStartAtToLocalDate();
        this.endAt = feedEntity.convertEndAtToLocalDate();
        this.records = projectionList.stream()
                .map(projection -> Record.builder()
                        .projection(projection)
                        .feedStartAt(feedEntity.getStartAt())
                        .build())
                .toList();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Record {
        private Long id;
        private Long dayNumber;
        private String title;
        private String place;
        private String imageUrl;
        private LocalDate date;

        @Builder
        private Record(RecordWithFeedProjection projection, LocalDateTime feedStartAt) {
            this.id = projection.getId();
            this.dayNumber = Duration.between(feedStartAt, projection.getDate()).toDays() + 1;
            this.title = projection.getTitle();
            this.place = projection.getPlace();
            this.imageUrl = projection.getImageUrl();
            this.date = projection.getDate().toLocalDate();
        }
    }
}
