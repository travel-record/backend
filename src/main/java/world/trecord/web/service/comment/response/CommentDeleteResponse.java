package world.trecord.web.service.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentDeleteResponse {
    private Long commentId;

    @Builder
    private CommentDeleteResponse(Long commentId) {
        this.commentId = commentId;
    }
}
