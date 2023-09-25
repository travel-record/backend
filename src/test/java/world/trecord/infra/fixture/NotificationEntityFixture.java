package world.trecord.infra.fixture;

import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;

public abstract class NotificationEntityFixture {

    public static NotificationEntity of(UserEntity userEntity, NotificationStatus notificationStatus) {
        return of(userEntity, null, null, null, notificationStatus, COMMENT);
    }

    public static NotificationEntity of(UserEntity userToEntity, UserEntity userFromEntity, RecordEntity recordEntity, CommentEntity commentEntity, NotificationStatus status, NotificationType type) {
        NotificationArgs args = NotificationArgs.builder()
                .commentEntity(commentEntity)
                .recordEntity(recordEntity)
                .userFromEntity(userFromEntity)
                .build();

        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .type(type)
                .status(status)
                .args(args)
                .build();
    }
}
