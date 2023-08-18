package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

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
    private String imageUrl;

    @Builder
    private RecordCreateResponse(UserEntity writerEntity, RecordEntity recordEntity) {
        this.writerId = writerEntity.getId();
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
        this.imageUrl = recordEntity.getImageUrl();
    }
}
