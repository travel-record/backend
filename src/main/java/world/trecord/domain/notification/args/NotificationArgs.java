package world.trecord.domain.notification.args;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

@NoArgsConstructor
@Getter
@Setter
public class NotificationArgs {

    private UserFromArgs userFrom;
    private FeedArgs feed;
    private RecordArgs record;
    private CommentArgs comment;

    @Builder
    private NotificationArgs(UserEntity userFromEntity,
                             FeedEntity feedEntity,
                             RecordEntity recordEntity,
                             CommentEntity commentEntity) {
        if (userFromEntity != null) {
            this.userFrom = new UserFromArgs(userFromEntity.getId(), userFromEntity.getNickname());
        }

        if (feedEntity != null) {
            this.feed = new FeedArgs(feedEntity.getId(), feedEntity.getName());
        }

        if (recordEntity != null) {
            this.record = new RecordArgs(recordEntity.getId(), recordEntity.getTitle());
        }

        if (commentEntity != null) {
            this.comment = new CommentArgs(commentEntity.getId(), commentEntity.getParentCommentId(), commentEntity.getContent());
        }
    }
}