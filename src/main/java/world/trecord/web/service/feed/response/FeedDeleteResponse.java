package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

@NoArgsConstructor
@Getter
@Setter
public class FeedDeleteResponse {
    private Long id;

    @Builder
    private FeedDeleteResponse(FeedEntity feedEntity) {
        this.id = feedEntity.getId();
    }
}
