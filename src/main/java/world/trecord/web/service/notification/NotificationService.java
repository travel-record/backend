package world.trecord.web.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.notification.NotificationType;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;

import java.util.List;

import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // TODO async 처리
    @Transactional
    public void createCommentNotification(CommentEntity commentEntity) {
        UserEntity userToEntity = commentEntity.getRecordEntity().getFeedEntity().getUserEntity();

        UserEntity userFromEntity = commentEntity.getUserEntity();

        if (userToEntity.isEqualTo(userFromEntity)) {
            return;
        }

        NotificationEntity notificationEntity = createCommentNotificationEntity(commentEntity, userToEntity, userFromEntity);

        notificationRepository.save(notificationEntity);
    }

    // TODO async 처리
    @Transactional
    public void createRecordLikeNotification(UserEntity userFromEntity, RecordEntity recordEntity) {
        UserEntity userToEntity = recordEntity.getFeedEntity().getUserEntity();

        if (userToEntity.isEqualTo(userFromEntity)) {
            return;
        }

        NotificationEntity notificationEntity = createRecordLikeNotificationEntity(recordEntity, userToEntity, userFromEntity);

        notificationRepository.save(notificationEntity);
    }

    public CheckNewNotificationResponse checkNewNotification(Long userId) {
        boolean hasNewNotification = notificationRepository.existsByUsersToEntityIdAndStatus(userId, UNREAD);

        return CheckNewNotificationResponse.builder()
                .hasNewNotification(hasNewNotification)
                .build();
    }

    @Transactional
    public NotificationListResponse getNotificationsOrException(Long userId) {
        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityIdOrderByCreatedDateTimeDesc(userId);

        NotificationListResponse response = NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();

        // TODO async 처리
        notificationRepository.updateNotificationStatusByUserId(userId, UNREAD, READ);

        return response;
    }

    public NotificationListResponse getNotificationsOrException(Long userId, NotificationType type) {
        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityIdAndTypeOrderByCreatedDateTimeDesc(userId, type);

        return NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();
    }

    private NotificationEntity createRecordLikeNotificationEntity(RecordEntity recordEntity, UserEntity userToEntity, UserEntity userFromEntity) {
        return NotificationEntity.builder()
                .recordEntity(recordEntity)
                .usersToEntity(userToEntity)
                .usersFromEntity(userFromEntity)
                .type(RECORD_LIKE)
                .status(UNREAD)
                .build();
    }

    private NotificationEntity createCommentNotificationEntity(CommentEntity commentEntity, UserEntity userToEntity, UserEntity userFromEntity) {
        return NotificationEntity.builder()
                .recordEntity(commentEntity.getRecordEntity())
                .commentEntity(commentEntity)
                .usersToEntity(userToEntity)
                .usersFromEntity(userFromEntity)
                .type(COMMENT)
                .status(UNREAD)
                .build();
    }
}
