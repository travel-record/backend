package world.trecord.domain.feed.projection;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class FeedWithRecordProjection {
    private Long writerId;
    private Long feedId;
    private String feedName;
    private String feedImageUrl;
    private String feedDescription;
    private LocalDate feedStartAt;
    private LocalDate feedEndAt;
    private String feedCompanion;
    private String feedPlace;
    private String feedSatisfaction;

    private Long recordId;
    private String recordTitle;
    private String recordPlace;
    private LocalDate recordDate;

    public FeedWithRecordProjection(Long writerId, Long feedId, String feedName, String feedImageUrl, String feedDescription, LocalDateTime feedStartAt, LocalDateTime feedEndAt
            , String feedCompanion, String feedPlace, String feedSatisfaction, Long recordId, String recordTitle, String recordPlace, LocalDateTime recordDate) {
        this.writerId = writerId;
        this.feedId = feedId;
        this.feedName = feedName;
        this.feedImageUrl = feedImageUrl;
        this.feedDescription = feedDescription;
        this.feedStartAt = covertToLocalDate(feedStartAt);
        this.feedEndAt = covertToLocalDate(feedEndAt);
        this.feedCompanion = feedCompanion;
        this.feedPlace = feedPlace;
        this.feedSatisfaction = feedSatisfaction;
        this.recordId = recordId;
        this.recordTitle = recordTitle;
        this.recordPlace = recordPlace;
        this.recordDate = covertToLocalDate(recordDate);
    }

    public LocalDate covertToLocalDate(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toLocalDate() : null;
    }
}
