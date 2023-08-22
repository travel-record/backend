package world.trecord.web.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.notification.NotificationType;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;

import java.util.List;

import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_USER;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationEntity createCommentNotification(CommentEntity commentEntity) {

        UserEntity userToEntity = commentEntity.getRecordEntity().getFeedEntity().getUserEntity();

        UserEntity userFromEntity = commentEntity.getUserEntity();

        if (isUserCommentingOnSelf(userToEntity, userFromEntity)) {
            return null;
        }

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .recordEntity(commentEntity.getRecordEntity())
                .commentEntity(commentEntity)
                .usersToEntity(userToEntity)
                .usersFromEntity(userFromEntity)
                .type(NotificationType.COMMENT)
                .status(UNREAD)
                .build();

        return notificationRepository.save(notificationEntity);
    }

    public CheckNewNotificationResponse checkNewNotificationBy(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        boolean hasNewNotification = notificationRepository.existsByUsersToEntityIdAndStatus(userEntity.getId(), UNREAD);

        return CheckNewNotificationResponse.builder()
                .hasNewNotification(hasNewNotification)
                .build();
    }

    @Transactional
    public NotificationListResponse getNotificationsBy(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityIdOrderByCreatedDateTimeDesc(userId);

        NotificationListResponse response = NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();

        notificationRepository.updateNotificationStatusByUserId(userEntity.getId(), UNREAD, READ);

        return response;
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private boolean isUserCommentingOnSelf(UserEntity userToEntity, UserEntity userFromEntity) {
        return userToEntity.equals(userFromEntity);
    }
}
