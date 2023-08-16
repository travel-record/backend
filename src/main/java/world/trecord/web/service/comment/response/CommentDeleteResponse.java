package world.trecord.web.service.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

@NoArgsConstructor
@Getter
@Setter
public class CommentDeleteResponse {
    private Long commentId;

    @Builder
    private CommentDeleteResponse(CommentEntity commentEntity) {
        this.commentId = commentEntity.getId();
    }
}
