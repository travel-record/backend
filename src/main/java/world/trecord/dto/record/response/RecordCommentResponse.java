package world.trecord.dto.record.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.comment.CommentEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class RecordCommentResponse {

    private Long commentId;
    private Boolean isUpdatable;
    private String content;
    private Long commenterId;
    private String commenterImageUrl;
    private String commenterNickname;
    private int replyCount;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime commentCreatedDate;

    public static RecordCommentResponse of(CommentEntity commentEntity, Long viewerId) {
        return RecordCommentResponse.builder()
                .commentEntity(commentEntity)
                .viewerId(viewerId)
                .build();
    }

    @Builder
    private RecordCommentResponse(CommentEntity commentEntity, Long viewerId) {
        this.commentId = commentEntity.getId();
        this.content = commentEntity.getContent();
        this.isUpdatable = commentEntity.isCommenter(viewerId);
        this.commentCreatedDate = commentEntity.getCreatedDateTime();
        this.commenterId = commentEntity.getUserEntity().getId();
        this.commenterImageUrl = commentEntity.getUserEntity().getImageUrl();
        this.commenterNickname = commentEntity.getUserEntity().getNickname();
        this.replyCount = commentEntity.getChildCommentEntities().size();
    }
}
