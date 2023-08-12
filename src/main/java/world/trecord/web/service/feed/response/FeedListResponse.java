package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class FeedListResponse {

    private List<Feed> feeds;

    @Builder
    private FeedListResponse(List<Feed> feeds) {
        this.feeds = feeds;
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Feed {
        private Long id;
        private String name;
        private String place;
        private String imageUrl;
        private LocalDate startAt;
        private LocalDate endAt;

        public Feed(FeedEntity feed) {
            this.id = feed.getId();
            this.name = feed.getName();
            this.place = feed.getPlace();
            this.imageUrl = feed.getImageUrl();
            this.startAt = feed.convertStartAtToLocalDate();
            this.endAt = feed.convertEndAtToLocalDate();
        }
    }
}

