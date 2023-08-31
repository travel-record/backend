package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class RecordInfoResponse {

    private Long writerId;
    private Long feedId;
    private Long recordId;
    private Boolean isUpdatable;
    private Boolean liked;
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
    private RecordInfoResponse(RecordEntity recordEntity, Long viewerId, Boolean liked) {
        this.writerId = recordEntity.getFeedEntity().getUserEntity().getId();
        this.feedId = recordEntity.getFeedEntity().getId();
        this.recordId = recordEntity.getId();
        this.isUpdatable = recordEntity.getFeedEntity().isManagedBy(viewerId);
        this.liked = liked;
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
