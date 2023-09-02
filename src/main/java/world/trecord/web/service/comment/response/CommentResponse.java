package world.trecord.web.service.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class CommentResponse {

    private Long recordId;
    private Long parentId;
    private Long commentId;
    private String content;
    private boolean isEditable;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdDateTime;

    @Builder
    private CommentResponse(CommentEntity commentEntity, Long viewerId) {
        this.recordId = commentEntity.getRecordEntity().getId();
        this.parentId = commentEntity.getParentCommentEntity() != null ? commentEntity.getParentCommentEntity().getId() : null;
        this.commentId = commentEntity.getId();
        this.content = commentEntity.getContent();
        this.isEditable = Objects.equals(viewerId, commentEntity.getUserEntity().getId());
        this.createdDateTime = commentEntity.getCreatedDateTime();
    }
}
