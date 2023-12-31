package world.trecord.event.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.event.sse.SseEmitterEvent;
import world.trecord.event.sse.SseEmitterService;
import world.trecord.service.notification.NotificationService;

import java.util.Objects;

@Slf4j
@Async
@RequiredArgsConstructor
@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @EventListener
    public void handleNotificationEventListener(NotificationEvent notificationEvent) {
        Long userToId = notificationEvent.userToId();
        Long userFromId = notificationEvent.userFromId();
        NotificationType type = notificationEvent.type();
        NotificationArgs args = notificationEvent.args();

        if (Objects.equals(userToId, userFromId)) {
            return;
        }

        NotificationEntity notificationEntity = notificationService.createNotification(userToId, type, args);
        log.info("NotificationEntity created with ID: [{}]", notificationEntity.getId());
        sseEmitterService.send(userToId, notificationEntity.getId(), buildSseEmitterEvent(notificationEntity));
    }

    private SseEmitterEvent buildSseEmitterEvent(NotificationEntity notificationEntity) {
        return SseEmitterEvent.builder()
                .notificationEntity(notificationEntity)
                .build();
    }
}
