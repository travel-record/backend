package world.trecord.domain.comment.projection;

import java.time.LocalDateTime;

public interface CommentRecordProjection {

    Long getRecordId();

    String getRecordTitle();

    Long getCommentId();

    String getContent();

    LocalDateTime getCreatedDateTime();
}
