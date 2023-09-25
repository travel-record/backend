package world.trecord.dto.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.dto.users.response.UserResponse;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class FeedInfoResponse {

    private Long writerId;
    private Long feedId;
    private Boolean canModifyFeed;
    private Boolean canWriteRecord;
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
    private List<UserResponse> contributors;

    public static FeedInfoResponse of(FeedEntity feedEntity, List<UserResponse> contributors, Long viewerId) {
        return FeedInfoResponse.builder()
                .feedEntity(feedEntity)
                .contributors(contributors)
                .viewerId(viewerId)
                .build();
    }

    @Builder
    private FeedInfoResponse(FeedEntity feedEntity, List<UserResponse> contributors, Long viewerId) {
        this.writerId = feedEntity.getUserId();
        this.contributors = contributors;
        this.feedId = feedEntity.getId();
        this.canModifyFeed = feedEntity.isOwnedBy(viewerId);
        this.canWriteRecord = feedEntity.canWriteRecord(viewerId);
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
