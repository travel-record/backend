package world.trecord.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.comment.projection.CommentRecordProjection;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class UserCommentResponse {

    private Long recordId;
    private String recordTitle;
    private Long commentId;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime commentCreatedDateTime;

    public static UserCommentResponse of(CommentRecordProjection projection) {
        return UserCommentResponse.builder()
                .projection(projection)
                .build();
    }

    @Builder
    private UserCommentResponse(CommentRecordProjection projection) {
        this.recordId = projection.getRecordId();
        this.recordTitle = projection.getRecordTitle();
        this.commentId = projection.getCommentId();
        this.content = projection.getContent();
        this.commentCreatedDateTime = projection.getCreatedDateTime();
    }
}
