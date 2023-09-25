package world.trecord.dto.feed.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDate;

@NoArgsConstructor
@Data
public class FeedListResponse {

    private Long id;
    private String name;
    private String place;
    private String longitude;
    private String latitude;
    private String imageUrl;
    private LocalDate startAt;
    private LocalDate endAt;

    public static FeedListResponse of(FeedEntity feedEntity) {
        return FeedListResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Builder
    private FeedListResponse(FeedEntity feedEntity) {
        this.id = feedEntity.getId();
        this.name = feedEntity.getName();
        this.place = feedEntity.getPlace();
        this.longitude = feedEntity.getLongitude();
        this.latitude = feedEntity.getLatitude();
        this.imageUrl = feedEntity.getImageUrl();
        this.startAt = feedEntity.convertStartAtToLocalDate();
        this.endAt = feedEntity.convertEndAtToLocalDate();
    }
}
