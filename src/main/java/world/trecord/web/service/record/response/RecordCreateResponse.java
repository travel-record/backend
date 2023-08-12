package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDate;

@NoArgsConstructor
@Setter
@Getter
public class RecordCreateResponse {

    private Long writerId;
    private Long feedId;
    private Long recordId;
    private String title;
    private LocalDate date;
    private String place;
    private String feeling;
    private String weather;
    private String transportation;
    private String content;
    private String companion;

    @Builder
    private RecordCreateResponse(Long writerId, RecordEntity recordEntity) {
        this.writerId = writerId;
        this.feedId = recordEntity.getFeedEntity().getId();
        this.recordId = recordEntity.getId();
        this.title = recordEntity.getTitle();
        this.date = recordEntity.convertDateToLocalDate();
        this.place = recordEntity.getPlace();
        this.feeling = recordEntity.getFeeling();
        this.weather = recordEntity.getWeather();
        this.transportation = recordEntity.getTransportation();
        this.content = recordEntity.getContent();
        this.companion = recordEntity.getCompanion();
    }
}
