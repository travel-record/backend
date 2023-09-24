package world.trecord.dto.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.Place;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class FeedListResponse {

    private List<Feed> feeds;

    @Builder
    private FeedListResponse(List<FeedEntity> feedEntities) {
        this.feeds = feedEntities.stream().map(Feed::new).toList();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Feed {
        private Long id;
        private String name;
        private Place place;
        private String imageUrl;
        private LocalDate startAt;
        private LocalDate endAt;

        public Feed(FeedEntity feedEntity) {
            this.id = feedEntity.getId();
            this.name = feedEntity.getName();
            this.place = feedEntity.getPlace();
            this.imageUrl = feedEntity.getImageUrl();
            this.startAt = feedEntity.convertStartAtToLocalDate();
            this.endAt = feedEntity.convertEndAtToLocalDate();
        }
    }
}

