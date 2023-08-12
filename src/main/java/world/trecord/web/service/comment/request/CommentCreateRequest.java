package world.trecord.web.service.comment.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotEmpty
    private String content;

    @Builder
    private CommentCreateRequest(Long recordId, String content) {
        this.recordId = recordId;
        this.content = content;
    }

    public CommentEntity toEntity(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }
}
