package world.trecord.dto.feed.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class FeedUpdateRequest {

    @NotEmpty
    private String name;

    private String satisfaction;

    private String place;

    private String longitude;

    private String latitude;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private String description;

    private String imageUrl;

    @Builder
    private FeedUpdateRequest(String name,
                              String satisfaction,
                              String place,
                              String longitude,
                              String latitude,
                              LocalDateTime startAt,
                              LocalDateTime endAt,
                              String description,
                              String imageUrl) {
        this.name = name;
        this.satisfaction = satisfaction;
        this.place = place;
        this.longitude = longitude;
        this.latitude = latitude;
        this.startAt = startAt;
        this.endAt = endAt;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public FeedEntity toUpdateEntity() {
        return FeedEntity.builder()
                .name(this.name)
                .satisfaction(this.satisfaction)
                .place(this.place)
                .longitude(this.longitude)
                .latitude(this.latitude)
                .startAt(this.startAt)
                .endAt(this.endAt)
                .description(this.description)
                .imageUrl(this.imageUrl)
                .build();
    }
}
