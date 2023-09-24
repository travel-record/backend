package world.trecord.dto.record.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.Place;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class RecordUpdateRequest {

    @NotEmpty
    private String title;

    @NotNull
    private LocalDateTime date;

    @NotNull
    private Place place;

    @NotEmpty
    private String feeling;

    @NotEmpty
    private String weather;

    @NotEmpty
    private String transportation;

    @NotEmpty
    private String content;

    private String imageUrl;

    private String companion;

    @Builder
    private RecordUpdateRequest(String title,
                                LocalDateTime date,
                                Place place,
                                String feeling,
                                String weather,
                                String transportation,
                                String content,
                                String companion,
                                String imageUrl) {
        this.title = title;
        this.date = date;
        this.place = place;
        this.feeling = feeling;
        this.weather = weather;
        this.transportation = transportation;
        this.content = content;
        this.companion = companion;
        this.imageUrl = imageUrl;
    }

    public RecordEntity toUpdateEntity() {
        return RecordEntity.builder()
                .title(this.title)
                .date(this.date)
                .place(this.place)
                .feeling(this.feeling)
                .weather(this.weather)
                .transportation(this.transportation)
                .content(this.content)
                .companion(this.companion)
                .imageUrl(this.imageUrl)
                .build();
    }
}
