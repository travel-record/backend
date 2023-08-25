package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

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
        private Long commenterId;
        private String commenterImageUrl;
        private Boolean isUpdatable;
        private String content;

        @Builder
        private Comment(CommentEntity commentEntity, Long viewerId) {
            this.commenterId = commentEntity.getUserEntity().getId();
            this.commentId = commentEntity.getId();
            this.commenterImageUrl = commentEntity.getUserEntity().getImageUrl();
            this.isUpdatable = commenterId.equals(viewerId);
            this.content = commentEntity.getContent();
        }
    }
}
