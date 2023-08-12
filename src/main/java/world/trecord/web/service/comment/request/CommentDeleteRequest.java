package world.trecord.web.service.comment.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentDeleteRequest {

    @NotNull
    private Long commentId;

    @Builder
    private CommentDeleteRequest(Long commentId) {
        this.commentId = commentId;
    }
}
