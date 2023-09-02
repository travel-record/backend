package world.trecord.web.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.notification.NotificationArgs;
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
import static world.trecord.web.exception.CustomExceptionError.USER_NOT_FOUND;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public CheckNewNotificationResponse checkUnreadNotifications(Long userId) {
        boolean hasNewNotification = notificationRepository.existsByUsersToEntityIdAndStatus(userId, UNREAD);

        return CheckNewNotificationResponse.builder()
                .hasNewNotification(hasNewNotification)
                .build();
    }

    @Transactional
    public NotificationEntity createNotification(Long userToId, NotificationType type, NotificationArgs args) {
        UserEntity userToEntity = userRepository.findById(userToId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        NotificationEntity notificationEntity = NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .args(args)
                .status(UNREAD)
                .type(type)
                .build();

        return notificationRepository.save(notificationEntity);
    }

    @Transactional
    public NotificationListResponse getNotifications(Long userId) {
        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityIdOrderByCreatedDateTimeDesc(userId);

        NotificationListResponse response = NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();

        markNotificationsAsRead(userId);

        return response;
    }

    @Transactional
    public NotificationListResponse getNotificationsByType(Long userId, NotificationType type) {
        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityIdAndTypeOrderByCreatedDateTimeDesc(userId, type);

        NotificationListResponse response = NotificationListResponse.builder()
                .notificationEntities(notificationList)
                .build();

        markNotificationsAsRead(userId);

        return response;
    }

    private void markNotificationsAsRead(Long userId) {
        notificationRepository.updateNotificationStatusByUserId(userId, UNREAD, READ);
    }
}
