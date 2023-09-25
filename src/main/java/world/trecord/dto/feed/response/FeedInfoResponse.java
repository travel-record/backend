package world.trecord.dto.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDate;

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
    private String longitude;
    private String latitude;
    private String satisfaction;
    private LocalDate startAt;
    private LocalDate endAt;

    public static FeedInfoResponse of(FeedEntity feedEntity, Long viewerId) {
        return FeedInfoResponse.builder()
                .feedEntity(feedEntity)
                .viewerId(viewerId)
                .build();
    }

    @Builder
    private FeedInfoResponse(FeedEntity feedEntity, Long viewerId) {
        this.writerId = feedEntity.getUserId();
        this.feedId = feedEntity.getId();
        this.isUpdatable = feedEntity.isOwnedBy(viewerId);
        this.name = feedEntity.getName();
        this.imageUrl = feedEntity.getImageUrl();
        this.description = feedEntity.getDescription();
        this.satisfaction = feedEntity.getSatisfaction();
        this.place = feedEntity.getPlace();
        this.longitude = feedEntity.getLongitude();
        this.latitude = feedEntity.getLatitude();
        this.companion = feedEntity.getCompanion();
        this.startAt = feedEntity.convertStartAtToLocalDate();
        this.endAt = feedEntity.convertEndAtToLocalDate();
    }
}
