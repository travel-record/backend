package world.trecord.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class CommentResponse {

    private Long commenterId;
    private String commenterImageUrl;
    private Long recordId;
    private Long parentId;
    private Long commentId;
    private String content;
    private boolean isUpdatable;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdDateTime;

    @Builder
    private CommentResponse(UserEntity userEntity, CommentEntity commentEntity, Long viewerId) {
        this.commenterId = userEntity.getId();
        this.commenterImageUrl = userEntity.getImageUrl();
        this.recordId = commentEntity.getRecordEntity().getId();
        this.parentId = commentEntity.getParentCommentId();
        this.commentId = commentEntity.getId();
        this.content = commentEntity.getContent();
        this.isUpdatable = commentEntity.isCommenter(viewerId);
        this.createdDateTime = commentEntity.getCreatedDateTime();
    }
}
