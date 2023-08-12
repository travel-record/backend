package world.trecord.web.service.record.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class RecordCreateRequest {

    @NotNull
    private Long feedId;

    @NotEmpty
    private String title;

    @NotNull
    private LocalDateTime date;

    @NotEmpty
    private String place;

    @NotEmpty
    private String feeling;

    @NotEmpty
    private String weather;

    @NotEmpty
    private String transportation;

    @NotEmpty
    private String content;

    private String companion;

    public RecordEntity toEntity(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(this.title)
                .date(this.date)
                .place(this.place)
                .feeling(this.feeling)
                .weather(this.weather)
                .transportation(this.transportation)
                .content(this.content)
                .companion(this.companion)
                .build();
    }

    @Builder
    private RecordCreateRequest(Long feedId, String title, LocalDateTime date, String place, String feeling, String weather, String transportation, String content, String companion) {
        this.feedId = feedId;
        this.title = title;
        this.date = date;
        this.place = place;
        this.feeling = feeling;
        this.weather = weather;
        this.transportation = transportation;
        this.content = content;
        this.companion = companion;
    }
}
