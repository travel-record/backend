package world.trecord.domain.notification;

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
public class NotificationArgs {
    private Long userFromId;
    private String userFromNickname;
    private Long commentId;
    private String commentContent;
    private Long recordId;

    @Builder
    private NotificationArgs(UserEntity userFromEntity, RecordEntity recordEntity, CommentEntity commentEntity) {
        if (userFromEntity != null) {
            this.userFromId = userFromEntity.getId();
            this.userFromNickname = userFromEntity.getNickname();
        }

        if (recordEntity != null) {
            this.recordId = recordEntity.getId();
        }

        if (commentEntity != null) {
            this.commentId = commentEntity.getId();
            this.commentContent = commentEntity.getContent();
        }
    }
}