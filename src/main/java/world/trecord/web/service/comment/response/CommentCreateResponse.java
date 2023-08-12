package world.trecord.web.service.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentCreateResponse {

    private Long recordId;
    private Long commentId;
    private String content;

    @Builder
    private CommentCreateResponse(Long recordId, Long commentId, String content) {
        this.recordId = recordId;
        this.commentId = commentId;
        this.content = content;
    }
}
