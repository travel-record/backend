package world.trecord.dto.feedcontributor.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserFeedContributorListResponse {

    private Long feedId;
    private String feedName;
    private String ownerNickname;
    private String imageUrl;

    public static UserFeedContributorListResponse fromEntity(FeedEntity feedEntity) {
        return new UserFeedContributorListResponse(
                feedEntity.getId(),
                feedEntity.getName(),
                feedEntity.getUserEntity().getNickname(),
                feedEntity.getImageUrl()
        );
    }
}
