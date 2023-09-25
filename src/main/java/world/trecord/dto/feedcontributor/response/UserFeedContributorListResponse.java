package world.trecord.dto.feedcontributor.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

@NoArgsConstructor
@Getter
@Setter
public class UserFeedContributorListResponse {

    private Long feedId;
    private String feedName;
    private String ownerNickname;
    private String imageUrl;

    public static UserFeedContributorListResponse fromEntity(FeedEntity feedEntity) {
        return UserFeedContributorListResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Builder
    private UserFeedContributorListResponse(FeedEntity feedEntity) {
        this.feedId = feedEntity.getId();
        this.feedName = feedEntity.getName();
        this.ownerNickname = feedEntity.getUserNickname();
        this.imageUrl = feedEntity.getImageUrl();
    }
}
