package world.trecord.web.service.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationType;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.notification.NotificationService;

import java.io.IOException;
import java.util.Objects;

import static world.trecord.web.exception.CustomExceptionError.NOTIFICATION_CONNECT_ERROR;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEmitterService {

    public final static String EVENT_NAME = "notification";

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationService notificationService;

    // TODO 비동기 처리

    @Transactional
    public void send(Long userToId, Long userFromId, NotificationType type, NotificationArgs args) {

        if (Objects.equals(userToId, userFromId)) {
            return;
        }

        NotificationEntity notificationEntity = notificationService.createNotification(userToId, type, args);

        sseEmitterRepository.findByUserId(userToId).ifPresentOrElse(it -> {
                    try {
                        it.send(SseEmitter.event()
                                .id(notificationEntity.getId().toString())
                                .name(EVENT_NAME)
                                .data(buildNotificationEvent(notificationEntity)));
                    } catch (IOException exception) {
                        sseEmitterRepository.delete(userToId);
                        throw new CustomException(NOTIFICATION_CONNECT_ERROR);
                    }
                },
                () -> log.info("No emitter founded")
        );
    }

    public SseEmitter connect(Long userId, SseEmitter emitter) {
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

    private SseEvent buildNotificationEvent(NotificationEntity notificationEntity) {
        return SseEvent.builder()
                .notificationEntity(notificationEntity)
                .build();
    }
}
