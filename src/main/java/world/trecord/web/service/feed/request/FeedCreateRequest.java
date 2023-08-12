package world.trecord.web.service.feed.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class FeedCreateRequest {

    @NotEmpty
    private String name;

    private String satisfaction;

    private String place;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private String companion;

    private String description;

    private String imageUrl;

    public FeedEntity toEntity(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name(this.name)
                .companion(this.companion)
                .satisfaction(this.satisfaction)
                .description(this.description)
                .imageUrl(this.imageUrl)
                .place(this.place)
                .startAt(this.startAt)
                .endAt(this.endAt)
                .build();
    }

    @Builder
    private FeedCreateRequest(String name, String satisfaction, String place, LocalDateTime startAt, LocalDateTime endAt, String companion, String description, String imageUrl) {
        this.name = name;
        this.satisfaction = satisfaction;
        this.place = place;
        this.startAt = startAt;
        this.endAt = endAt;
        this.companion = companion;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
