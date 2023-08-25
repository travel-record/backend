package world.trecord.web.service.record.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class RecordCommentsResponse {

    private List<Comment> comments;

    @Builder
    private RecordCommentsResponse(List<CommentEntity> commentEntities, Long viewerId) {
        this.comments = commentEntities.stream().map(commentEntity ->
                        Comment.builder()
                                .commentEntity(commentEntity)
                                .viewerId(viewerId)
                                .build())
                .toList();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Comment {
        private Long commentId;
        private Boolean isUpdatable;
        private String content;
        private Long commenterId;
        private String commenterImageUrl;
        private String commenterNickname;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime commentCreatedDate;

        @Builder
        private Comment(CommentEntity commentEntity, Long viewerId) {
            this.commentId = commentEntity.getId();
            this.content = commentEntity.getContent();
            this.isUpdatable = commentEntity.getUserEntity().getId().equals(viewerId);
            this.commentCreatedDate = commentEntity.getCreatedDateTime();
            this.commenterId = commentEntity.getUserEntity().getId();
            this.commenterImageUrl = commentEntity.getUserEntity().getImageUrl();
            this.commenterNickname = commentEntity.getUserEntity().getNickname();
        }
    }
}
