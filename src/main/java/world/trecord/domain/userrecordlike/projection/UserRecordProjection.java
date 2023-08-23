package world.trecord.domain.userrecordlike.projection;

public interface UserRecordProjection {

    Long getId();

    String getTitle();

    String getImageUrl();

    Long getAuthorId();

    String getAuthorNickname();
}
