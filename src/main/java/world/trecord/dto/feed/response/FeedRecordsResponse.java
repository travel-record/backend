package world.trecord.dto.feed.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.projection.RecordWithFeedProjection;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class FeedRecordsResponse {
    private Long id;
    private Long dayNumber;
    private String title;
    private String place;
    private String imageUrl;
    private LocalDate date;

    @Builder
    private FeedRecordsResponse(RecordWithFeedProjection projection, LocalDateTime feedStartAt) {
        this.id = projection.getId();
        this.dayNumber = Duration.between(feedStartAt, projection.getDate()).toDays() + 1;
        this.title = projection.getTitle();
        this.place = projection.getPlace();
        this.imageUrl = projection.getImageUrl();
        this.date = projection.getDate().toLocalDate();
    }
}
