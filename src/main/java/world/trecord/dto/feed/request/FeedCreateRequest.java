package world.trecord.dto.feed.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class FeedCreateRequest {

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

    private List<Long> contributors = new ArrayList<>();

    public FeedEntity toEntity(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name(this.name)
                .satisfaction(this.satisfaction)
                .description(this.description)
                .imageUrl(this.imageUrl)
                .place(this.place)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .startAt(this.startAt)
                .endAt(this.endAt)
                .build();
    }

    @Builder
    private FeedCreateRequest(String name,
                              String satisfaction,
                              String place,
                              String longitude,
                              String latitude,
                              LocalDateTime startAt,
                              LocalDateTime endAt,
                              String description,
                              String imageUrl,
                              List<Long> contributors) {
        this.name = name;
        this.satisfaction = satisfaction;
        this.place = place;
        this.longitude = longitude;
        this.latitude = latitude;
        this.startAt = startAt;
        this.endAt = endAt;
        this.description = description;
        this.imageUrl = imageUrl;
        this.contributors = contributors;
    }
}
