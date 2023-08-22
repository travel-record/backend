package world.trecord.web.service.users.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class UserCommentsResponse {
    private List<Comment> comments;

    @Builder
    private UserCommentsResponse(List<CommentEntity> commentEntities) {
        this.comments = commentEntities.stream().map(Comment::new).toList();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Comment {
        private Long recordId;
        private Long commentId;
        private String content;

        public Comment(CommentEntity commentEntity) {
            this.recordId = commentEntity.getRecordEntity().getId();
            this.commentId = commentEntity.getId();
            this.content = commentEntity.getContent();
        }
    }
}
