package world.trecord.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.notification.response.CheckNewNotificationResponse;
import world.trecord.dto.notification.response.NotificationResponse;
import world.trecord.exception.CustomException;

import static world.trecord.domain.notification.enumeration.NotificationStatus.READ;
import static world.trecord.domain.notification.enumeration.NotificationStatus.UNREAD;
import static world.trecord.exception.CustomExceptionError.NOTIFICATION_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.USER_NOT_FOUND;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public CheckNewNotificationResponse checkUnreadNotifications(Long userId) {
        boolean hasNewNotification = notificationRepository.existsByUsersToEntityIdAndStatus(userId, UNREAD);
        return buildNewNotificationResponse(hasNewNotification);
    }

    @Transactional
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        Page<NotificationEntity> notifications = notificationRepository.findByUsersToEntityId(userId, pageable);
        markNotificationsAsRead(userId);
        return notifications.map(NotificationResponse::of);
    }

    @Transactional
    public Page<NotificationResponse> getNotificationsByType(Long userId, NotificationType type, Pageable pageable) {
        Page<NotificationEntity> notifications = notificationRepository.findByUsersToEntityIdAndType(userId, type, pageable);
        markNotificationsAsRead(userId);
        return notifications.map(NotificationResponse::of);
    }

    @Transactional
    public NotificationEntity createNotification(Long userToId, NotificationType type, NotificationArgs args) {
        UserEntity userToEntity = userRepository.findById(userToId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return notificationRepository.save(buildNotificationEntity(type, args, userToEntity));
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        NotificationEntity notificationEntity = notificationRepository.findByIdAndUsersToEntityId(notificationId, userId)
                .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));
        notificationRepository.delete(notificationEntity);
    }

    private void markNotificationsAsRead(Long userId) {
        notificationRepository.updateNotificationStatusByUserId(userId, UNREAD, READ);
    }

    private CheckNewNotificationResponse buildNewNotificationResponse(boolean hasNewNotification) {
        return CheckNewNotificationResponse.builder()
                .hasNewNotification(hasNewNotification)
                .build();
    }

    private NotificationEntity buildNotificationEntity(NotificationType type, NotificationArgs args, UserEntity userToEntity) {
        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .args(args)
                .status(UNREAD)
                .type(type)
                .build();
    }
}
