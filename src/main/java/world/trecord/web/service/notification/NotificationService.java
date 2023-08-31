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
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;

import java.util.List;

import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;
import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_USER;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createCommentNotification(CommentEntity commentEntity) {

        UserEntity userToEntity = commentEntity.getRecordEntity().getFeedEntity().getUserEntity();

        UserEntity userFromEntity = commentEntity.getUserEntity();

        if (isUserCommentingOnSelf(userToEntity, userFromEntity)) {
            return;
        }

        NotificationEntity notificationEntity = createCommentNotificationEntity(commentEntity, userToEntity, userFromEntity);

        notificationRepository.save(notificationEntity);
    }

    @Transactional
    public void createRecordLikeNotification(RecordEntity recordEntity, UserEntity userFromEntity) {

        UserEntity userToEntity = recordEntity.getFeedEntity().getUserEntity();

        if (isUserLikeOnSelf(userToEntity, userFromEntity)) {
            return;
        }

        NotificationEntity notificationEntity = createRecordLikeNotificationEntity(recordEntity, userToEntity, userFromEntity);

        notificationRepository.save(notificationEntity);
    }

    public CheckNewNotificationResponse checkNewNotification(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        boolean hasNewNotification = notificationRepository.existsByUsersToEntityIdAndStatus(userEntity.getId(), UNREAD);

        return CheckNewNotificationResponse.builder()
                .hasNewNotification(hasNewNotification)
                .build();
    }

    @Transactional
    public NotificationListResponse getNotifications(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityOrderByCreatedDateTimeDesc(userEntity);

        NotificationListResponse response = NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();

        // TODO async 처리
        notificationRepository.updateNotificationStatusByUserId(userEntity.getId(), UNREAD, READ);

        return response;
    }

    public NotificationListResponse getNotifications(Long userId, NotificationType type) {
        UserEntity userEntity = findUserEntityBy(userId);

        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityAndTypeOrderByCreatedDateTimeDesc(userEntity, type);

        return NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private boolean isUserLikeOnSelf(UserEntity userToEntity, UserEntity userFromEntity) {
        return userToEntity.equals(userFromEntity);
    }

    private boolean isUserCommentingOnSelf(UserEntity userToEntity, UserEntity userFromEntity) {
        return userToEntity.equals(userFromEntity);
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
