package world.trecord.web.service.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDate;

@NoArgsConstructor
@Setter
@Getter
public class FeedCreateResponse {

    private Long writerId;
    private Long feedId;
    private String name;
    private String imageUrl;
    private String description;
    private String companion;
    private String place;
    private String satisfaction;
    private LocalDate startAt;
    private LocalDate endAt;

    @Builder
    private FeedCreateResponse(FeedEntity feedEntity) {
        this.writerId = feedEntity.getUserEntity().getId();
        this.feedId = feedEntity.getId();
        this.name = feedEntity.getName();
        this.imageUrl = feedEntity.getImageUrl();
        this.description = feedEntity.getDescription();
        this.satisfaction = feedEntity.getSatisfaction();
        this.place = feedEntity.getPlace();
        this.companion = feedEntity.getCompanion();
        this.startAt = feedEntity.convertStartAtToLocalDate();
        this.endAt = feedEntity.convertEndAtToLocalDate();
    }
}
