package world.trecord.web.service.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

@NoArgsConstructor
@Getter
@Setter
public class CommentUpdateResponse {

    private Long recordId;
    private Long commentId;
    private Long parentId;
    private String content;

    @Builder
    private CommentUpdateResponse(CommentEntity commentEntity) {
        this.recordId = commentEntity.getRecordEntity().getId();
        this.commentId = commentEntity.getId();
        this.parentId = commentEntity.getParentCommentEntity() != null ? commentEntity.getParentCommentEntity().getId() : null;
        this.content = commentEntity.getContent();
    }
}
