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
import java.util.concurrent.atomic.AtomicInteger;

import static world.trecord.web.exception.CustomExceptionError.MAX_CONNECTIONS_EXCEEDED_ERROR;
import static world.trecord.web.exception.CustomExceptionError.NOTIFICATION_CONNECT_ERROR;

// TODO 비동기 처리
@Slf4j
@RequiredArgsConstructor
@Component
public class SseEmitterService {

    public final static String EVENT_NAME = "notification";
    private static final int MAX_CONNECTIONS = 1000;
    private AtomicInteger currentConnections = new AtomicInteger(0);

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationService notificationService;

    @Transactional
    public void send(Long userToId, Long userFromId, NotificationType type, NotificationArgs args) {
        if (Objects.equals(userToId, userFromId)) {
            return;
        }

        NotificationEntity notificationEntity = notificationService.createNotification(userToId, type, args);

        sseEmitterRepository.findByUserId(userToId).ifPresentOrElse(emitter -> {
                    try {
                        emitter.send(SseEmitter.event()
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
        synchronized (this) {
            if (currentConnections.get() >= MAX_CONNECTIONS) {
                throw new CustomException(MAX_CONNECTIONS_EXCEEDED_ERROR);
            }
            doIncrementConnection();
        }

        try {
            doEstablishConnection(userId, emitter);
            doSendConnectionCompletionEvent(userId, emitter);
        } catch (Exception e) {
            doDecrementConnection();
            log.error("Error establishing SSE connection for user {}: {}", userId, e.getMessage());
            throw new CustomException(NOTIFICATION_CONNECT_ERROR);
        }

        return emitter;
    }

    private void doEstablishConnection(Long userId, SseEmitter emitter) {
        sseEmitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE connection for user {} has been completed.", userId);
            doReleaseExternalResources(userId);
            doDecrementConnection();
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection for user {} has timed out.", userId);
            doReleaseExternalResources(userId);
            doDecrementConnection();
        });

        emitter.onError((throwable) -> {
            log.error("Error with SSE connection for user {}: {}", userId, throwable.getMessage());
            doReleaseExternalResources(userId);
            doDecrementConnection();
        });

        // 연결이 끊어진 경우도 처리해줘야함
    }

    private void doReleaseExternalResources(Long userId) {
        sseEmitterRepository.delete(userId);
    }

    private void doSendConnectionCompletionEvent(Long userId, SseEmitter emitter) throws IOException {
        log.info("Attempting to send connection completion event for user {}.", userId);
        String eventID = String.valueOf(System.currentTimeMillis());
        emitter.send(SseEmitter.event()
                .id(eventID)
                .name(EVENT_NAME)
                .data("Connection completed"));
        log.info("Connection completion event for user {} has been sent successfully.", userId);
    }

    private void doIncrementConnection() {
        currentConnections.incrementAndGet();
    }

    private void doDecrementConnection() {
        currentConnections.decrementAndGet();
    }

    private SseNotificationEvent buildNotificationEvent(NotificationEntity notificationEntity) {
        return SseNotificationEvent.builder()
                .notificationEntity(notificationEntity)
                .build();
    }
}
