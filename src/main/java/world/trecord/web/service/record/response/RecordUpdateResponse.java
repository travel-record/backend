package world.trecord.web.service.record.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class RecordUpdateResponse {

    private Long feedId;

    private Long recordId;

    private String title;

    private LocalDateTime date;

    private String place;

    private String feeling;

    private String weather;

    private String transportation;

    private String content;

    private String companion;

    public RecordUpdateResponse(Long feedId, Long recordId, String title, LocalDateTime date, String place, String feeling, String weather, String transportation, String content, String companion) {
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
    }
}
