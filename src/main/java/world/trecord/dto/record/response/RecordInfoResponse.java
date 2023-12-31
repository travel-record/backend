package world.trecord.dto.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;
import world.trecord.dto.users.response.UserResponse;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class RecordInfoResponse {

    private Long writerId;
    private Long feedId;
    private Long recordId;
    private Boolean canModifyRecord;
    private Boolean liked;
    private String title;
    private LocalDate date;
    private String place;
    private String latitude;
    private String longitude;
    private String feeling;
    private String weather;
    private String transportation;
    private String content;
    private String imageUrl;
    private UserResponse author;

    public static RecordInfoResponse of(RecordEntity recordEntity, Long viewerId, Boolean liked) {
        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(viewerId)
                .liked(liked)
                .build();
    }

    @Builder
    private RecordInfoResponse(RecordEntity recordEntity, Long viewerId, Boolean liked) {
        this.writerId = recordEntity.getFeedEntity().getUserId();
        this.feedId = recordEntity.getFeedId();
        this.recordId = recordEntity.getId();
        this.canModifyRecord = recordEntity.isUpdatable(viewerId);
        this.liked = liked;
        this.title = recordEntity.getTitle();
        this.date = recordEntity.convertDateToLocalDate();
        this.latitude = recordEntity.getLatitude();
        this.longitude = recordEntity.getLongitude();
        this.place = recordEntity.getPlace();
        this.feeling = recordEntity.getFeeling();
        this.weather = recordEntity.getWeather();
        this.transportation = recordEntity.getTransportation();
        this.content = recordEntity.getContent();
        this.imageUrl = recordEntity.getImageUrl();
        this.author = UserResponse.of(recordEntity.getUserEntity());
    }
}
