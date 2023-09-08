package world.trecord.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.service.sse.SseEmitterEvent;
import world.trecord.service.sse.SseEmitterService;

import java.util.Objects;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Async
@RequiredArgsConstructor
@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @EventListener
    @Transactional(propagation = REQUIRES_NEW)
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
        sseEmitterService.send(notificationEntity.getId(), buildSseEmitterEvent(notificationEntity));
    }

    private SseEmitterEvent buildSseEmitterEvent(NotificationEntity notificationEntity) {
        return SseEmitterEvent.builder()
                .notificationEntity(notificationEntity)
                .build();
    }
}
