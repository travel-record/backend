package world.trecord.web.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.notification.NotificationType;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;
import static world.trecord.web.exception.CustomExceptionError.NOTIFICATION_CONNECT_ERROR;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    public final static String EVENT_NAME = "notification";

    private final SseEmitterRepository sseEmitterRepository;
    private final SseEmitterService sseEmitterService;
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

    public SseEmitter connectNotification(Long userId, Duration duration) {
        SseEmitter emitter = sseEmitterService.createSseEmitter(duration);

        sseEmitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE connection for user {} has been completed.", userId);
            sseEmitterRepository.delete(userId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection for user {} has timed out.", userId);
            sseEmitterRepository.delete(userId);
        });


        try {
            log.info("Attempting to send connection completion event for user {}.", userId);
            String eventID = String.valueOf(System.currentTimeMillis());
            emitter.send(SseEmitter.event()
                    .id(eventID)
                    .name(EVENT_NAME)
                    .data("Connection completed"));
            log.info("Connection completion event for user {} has been sent successfully.", userId);
        } catch (IOException exception) {
            log.error("Error sending connection completion event for user {}: {}", userId, exception.getMessage());
            throw new CustomException(NOTIFICATION_CONNECT_ERROR);
        }

        return emitter;
    }

    @Transactional
    public NotificationListResponse getNotifications(Long userId) {
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
        NotificationArgs args = NotificationArgs.builder()
                .recordEntity(recordEntity)
                .userFromEntity(userFromEntity)
                .build();

        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .args(args)
                .type(RECORD_LIKE)
                .status(UNREAD)
                .build();
    }

    private NotificationEntity createCommentNotificationEntity(CommentEntity commentEntity, UserEntity userToEntity, UserEntity userFromEntity) {
        NotificationArgs args = NotificationArgs.builder()
                .commentEntity(commentEntity)
                .recordEntity(commentEntity.getRecordEntity())
                .userFromEntity(userFromEntity)
                .build();

        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .type(COMMENT)
                .status(UNREAD)
                .args(args)
                .build();
    }
}
