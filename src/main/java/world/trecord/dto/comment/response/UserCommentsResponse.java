package world.trecord.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.projection.CommentRecordProjection;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class UserCommentsResponse {
    private List<Comment> comments;

    @Builder
    private UserCommentsResponse(List<CommentRecordProjection> projectionList) {
        this.comments = projectionList.stream().map(Comment::new).toList();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Comment {
        private Long recordId;
        private String recordTitle;
        private Long commentId;
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime commentCreatedDateTime;

        public Comment(CommentRecordProjection projection) {
            this.recordId = projection.getRecordId();
            this.recordTitle = projection.getRecordTitle();
            this.commentId = projection.getCommentId();
            this.content = projection.getContent();
            this.commentCreatedDateTime = projection.getCreatedDateTime();
        }
    }
}
