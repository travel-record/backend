package world.trecord.web.service.record.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class RecordUpdateRequest {

    @NotNull
    private Long feedId;

    @NotNull
    private Long recordId;

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

    @NotEmpty
    private String imageUrl;

    private String companion;

    @Builder
    private RecordUpdateRequest(Long feedId, Long recordId, String title, LocalDateTime date, String place, String feeling, String weather, String transportation, String content, String companion, String imageUrl) {
        this.feedId = feedId;
        this.recordId = recordId;
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
}
