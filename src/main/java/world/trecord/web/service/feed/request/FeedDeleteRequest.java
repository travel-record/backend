package world.trecord.web.service.feed.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class FeedDeleteRequest {
    @NotNull
    private Long id;

    @Builder
    private FeedDeleteRequest(Long id) {
        this.id = id;
    }
}
