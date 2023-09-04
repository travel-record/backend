package world.trecord.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

@NoArgsConstructor
@Setter
@Getter
public class FeedCreateResponse {

    private Long writerId;
    private Long feedId;

    @Builder
    private FeedCreateResponse(FeedEntity feedEntity) {
        this.writerId = feedEntity.getUserEntity().getId();
        this.feedId = feedEntity.getId();
    }
}
