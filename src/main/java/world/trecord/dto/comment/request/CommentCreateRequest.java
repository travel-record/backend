package world.trecord.dto.comment.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

@NoArgsConstructor
@Getter
@Setter
public class CommentCreateRequest {

    @NotNull
    private Long recordId;

    private Long parentId;

    @Size(max = 255)
    @NotEmpty
    private String content;

    @Builder
    private CommentCreateRequest(Long recordId, Long parentId, String content) {
        this.recordId = recordId;
        this.parentId = parentId;
        this.content = content;
    }

    public CommentEntity toEntity(UserEntity userEntity, RecordEntity recordEntity, CommentEntity parentCommentEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .parentCommentEntity(parentCommentEntity)
                .content(content)
                .build();
    }
}
