package world.trecord.service.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationType;
import world.trecord.exception.CustomException;
import world.trecord.service.notification.NotificationService;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static world.trecord.exception.CustomExceptionError.MAX_CONNECTIONS_EXCEEDED_ERROR;
import static world.trecord.exception.CustomExceptionError.NOTIFICATION_CONNECT_ERROR;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEmitterService {

    public final static String EVENT_NAME = "notification";
    private static final int MAX_CONNECTIONS = 1000;
    private AtomicInteger currentConnections = new AtomicInteger(0);

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationService notificationService;

    @Async("sseTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(Long userToId, Long userFromId, NotificationType type, NotificationArgs args) {
        // TODO
//        if (Objects.equals(userToId, userFromId)) {
//            return;
//        }

        log.info("Starting send method with userToId: [{}], userFromId: [{}]", userToId, userFromId);

        NotificationEntity notificationEntity = notificationService.createNotification(userToId, type, args);
        log.info("NotificationEntity created with ID: [{}]", notificationEntity.getId());

        sseEmitterRepository.findByUserId(userToId).ifPresentOrElse(emitter -> {
                    try {
                        log.info("Emitter found for userToId: [{}]. Sending notification...", userToId);
                        emitter.send(SseEmitter.event()
                                .id(notificationEntity.getId().toString())
                                .name(EVENT_NAME)
                                .data(doBuildNotificationEvent(notificationEntity)));
                        log.info("Successfully sent notification with ID: [{}] to emitter for userToId: [{}]", notificationEntity.getId(), userToId);

                    } catch (IOException ex) {
                        log.error("Error while sending notification to emitter for userToId: [{}]. Removing emitter.", userToId, ex);
                        releaseExternalResources(userFromId);
                        throw new CustomException(NOTIFICATION_CONNECT_ERROR);
                    }
                },
                () -> log.info("No emitter found for userToId: [{}]", userToId)
        );

        log.info("Finished send method for userToId: [{}] and userFromId: [{}]", userToId, userFromId);
    }

    public SseEmitter connect(Long userId, SseEmitter emitter) {
        synchronized (this) {
            if (currentConnections.get() >= MAX_CONNECTIONS) {
                log.warn("Max connections limit reached. Unable to connect user [{}].", userId);
                throw new CustomException(MAX_CONNECTIONS_EXCEEDED_ERROR);
            }
            doIncrementConnection();
            log.info("Connection incremented for user [{}]. Current connections: {}", userId, currentConnections.get());
        }

        try {
            establishConnection(userId, emitter);
            doSendConnectionCompletionEvent(userId, emitter);
        } catch (Exception e) {
            doDecrementConnection();
            log.error("Error establishing SSE connection for user [{}]: {}", userId, e.getMessage());
            throw new CustomException(NOTIFICATION_CONNECT_ERROR);
        }

        return emitter;
    }

    private void establishConnection(Long userId, SseEmitter emitter) {
        sseEmitterRepository.save(userId, emitter);
        log.info("SSE connection established and saved for user [{}].", userId);

        emitter.onCompletion(() -> {
            log.info("SSE connection for user [{}] has been completed.", userId);
            releaseExternalResources(userId);
            doDecrementConnection();
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection for user [{}] has timed out.", userId);
            releaseExternalResources(userId);
            doDecrementConnection();
        });

        emitter.onError((throwable) -> {
            log.error("Error with SSE connection for user [{}]: {}", userId, throwable.getMessage());
            releaseExternalResources(userId);
            doDecrementConnection();
        });

        // 연결이 끊어진 경우도 처리해줘야함
    }

    private void releaseExternalResources(Long userId) {
        sseEmitterRepository.delete(userId);
        log.info("External resources released and SSE emitter removed from the repository for user [{}].", userId);
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

    private SseNotificationEvent doBuildNotificationEvent(NotificationEntity notificationEntity) {
        return SseNotificationEvent.builder()
                .notificationEntity(notificationEntity)
                .build();
    }
}
