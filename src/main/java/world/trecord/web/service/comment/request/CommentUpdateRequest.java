package world.trecord.web.service.comment.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

@NoArgsConstructor
@Getter
@Setter
public class CommentUpdateRequest {

    @NotNull
    private Long commentId;

    @NotEmpty
    private String content;

    @Builder
    private CommentUpdateRequest(Long commentId, String content) {
        this.commentId = commentId;
        this.content = content;
    }

    public CommentEntity toUpdateEntity() {
        return CommentEntity.builder()
                .content(this.content)
                .build();
    }
}
