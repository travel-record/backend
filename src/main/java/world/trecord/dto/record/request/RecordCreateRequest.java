package world.trecord.dto.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

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

    @NotBlank
    private String place;

    @NotBlank
    private String latitude;

    @NotBlank
    private String longitude;

    @NotEmpty
    private String feeling;

    @NotEmpty
    private String weather;

    @NotEmpty
    private String transportation;

    @NotEmpty
    private String content;

    private String imageUrl;

    @Builder
    private RecordCreateRequest(Long feedId,
                                String title,
                                LocalDateTime date,
                                String place,
                                String longitude,
                                String latitude,
                                String feeling,
                                String weather,
                                String transportation,
                                String content,
                                String imageUrl) {
        this.feedId = feedId;
        this.title = title;
        this.date = date;
        this.place = place;
        this.longitude = longitude;
        this.latitude = latitude;
        this.feeling = feeling;
        this.weather = weather;
        this.transportation = transportation;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public RecordEntity toEntity(UserEntity userEntity, FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .title(this.title)
                .date(this.date)
                .place(this.place)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .feeling(this.feeling)
                .weather(this.weather)
                .transportation(this.transportation)
                .content(this.content)
                .sequence(sequence)
                .imageUrl(this.imageUrl)
                .build();
    }
}
