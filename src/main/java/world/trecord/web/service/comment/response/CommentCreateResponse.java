package world.trecord.web.service.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.record.RecordEntity;

@NoArgsConstructor
@Getter
@Setter
public class CommentCreateResponse {

    private Long recordId;
    private Long commentId;
    private String content;

    @Builder
    private CommentCreateResponse(RecordEntity recordEntity, CommentEntity commentEntity) {
        this.recordId = recordEntity.getId();
        this.commentId = commentEntity.getId();
        this.content = commentEntity.getContent();
    }
}
