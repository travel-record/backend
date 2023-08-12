package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FeedDeleteResponse {
    private Long id;

    @Builder
    private FeedDeleteResponse(Long id) {
        this.id = id;
    }
}
