package world.trecord.domain.comment.projection;

public interface CommentRecordProjection {

    Long getRecordId();

    String getRecordTitle();

    Long getCommentId();

    String getContent();
}
