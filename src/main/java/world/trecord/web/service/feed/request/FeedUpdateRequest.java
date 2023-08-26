package world.trecord.web.service.feed.request;

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

    @NotNull
    private Long id;

    @NotEmpty
    private String name;

    private String satisfaction;

    private String place;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private String companion;

    private String description;

    private String imageUrl;

    @Builder
    private FeedUpdateRequest(Long id, String name, String satisfaction, String place, LocalDateTime startAt, LocalDateTime endAt, String companion, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.satisfaction = satisfaction;
        this.place = place;
        this.startAt = startAt;
        this.endAt = endAt;
        this.companion = companion;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public FeedEntity toUpdateEntity() {
        return FeedEntity.builder()
                .name(this.name)
                .satisfaction(this.satisfaction)
                .place(this.place)
                .startAt(this.startAt)
                .endAt(this.endAt)
                .companion(this.companion)
                .description(this.description)
                .imageUrl(this.imageUrl)
                .build();
    }
}
