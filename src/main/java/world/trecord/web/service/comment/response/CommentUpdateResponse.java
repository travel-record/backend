package world.trecord.web.service.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentUpdateResponse {

    private Long recordId;
    private Long commentId;
    private String content;

    @Builder
    private CommentUpdateResponse(Long recordId, Long commentId, String content) {
        this.recordId = recordId;
        this.commentId = commentId;
        this.content = content;
    }
}
