package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class RecordInfoResponse {

    private Long writerId;
    private Long feedId;
    private Long recordId;
    private Boolean isUpdatable;
    private String title;
    private LocalDate date;
    private String place;
    private String feeling;
    private String weather;
    private String transportation;
    private String content;
    private String companion;
    private String imageUrl;
    private List<Comment> comments;

    @Builder
    private RecordInfoResponse(RecordEntity recordEntity, Long viewerId) {
        this.writerId = recordEntity.getFeedEntity().getUserEntity().getId();
        this.feedId = recordEntity.getFeedEntity().getId();
        this.recordId = recordEntity.getId();
        this.isUpdatable = writerId.equals(viewerId);
        this.title = recordEntity.getTitle();
        this.date = recordEntity.convertDateToLocalDate();
        this.place = recordEntity.getPlace();
        this.feeling = recordEntity.getFeeling();
        this.weather = recordEntity.getWeather();
        this.transportation = recordEntity.getTransportation();
        this.content = recordEntity.getContent();
        this.companion = recordEntity.getCompanion();
        this.imageUrl = recordEntity.getImageUrl();
        this.comments = recordEntity.sortCommentEntityByCreatedDateTimeAsc()
                .map(c -> Comment.builder()
                        .commenterId(c.getUserEntity().getId())
                        .commentId(c.getId())
                        .content(c.getContent())
                        .viewerId(viewerId)
                        .build())
                .toList();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Comment {
        private Long commentId;
        private Long commenterId;
        private Boolean isUpdatable;
        private String content;

        @Builder
        private Comment(Long commenterId, Long commentId, String content, Long viewerId) {
            this.commenterId = commenterId;
            this.commentId = commentId;
            this.isUpdatable = commenterId.equals(viewerId);
            this.content = content;
        }
    }

}
