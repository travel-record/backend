package world.trecord.dto.comment.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

@NoArgsConstructor
@Getter
@Setter
public class CommentUpdateRequest {

    @Size(max = 255)
    @NotEmpty
    private String content;

    @Builder
    private CommentUpdateRequest(String content) {
        this.content = content;
    }

    public CommentEntity toUpdateEntity() {
        return CommentEntity.builder()
                .content(this.content)
                .build();
    }
}
