package world.trecord.event.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.exception.CustomException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static world.trecord.exception.CustomExceptionError.MAX_CONNECTIONS_EXCEEDED_ERROR;
import static world.trecord.exception.CustomExceptionError.NOTIFICATION_CONNECT_ERROR;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEmitterService {

    public static final String EVENT_NAME = "notification";
    private static final int MAX_CONNECTIONS = 1000;
    private final AtomicInteger currentConnections = new AtomicInteger(0);
    private final SseEmitterRepository sseEmitterRepository;

    public void send(Long userToId, Long eventId, SseEmitterEvent sseEmitterEvent) {
        log.info("Starting send sse event with userToId: [{}]");

        sseEmitterRepository.findByUserId(userToId).ifPresentOrElse(emitter -> {
                    try {
                        log.info("Emitter found for userToId: [{}]. Sending notification...", userToId);
                        emitter.send(SseEmitter.event()
                                .id(eventId.toString())
                                .name(EVENT_NAME)
                                .data(sseEmitterEvent));
                        log.info("Successfully sent notification with ID: [{}] to emitter for userToId: [{}]", eventId, userToId);
                    } catch (IOException ex) {
                        log.error("Error while sending notification to emitter for userToId: [{}]. Removing emitter.", userToId, ex);
                        releaseExternalResources(userToId);
                        throw new CustomException(NOTIFICATION_CONNECT_ERROR);
                    }
                },
                () -> log.info("No emitter found for userToId: [{}]", userToId)
        );

        log.info("Finished send sse for userToId: [{}]", userToId);
    }

    public SseEmitter connect(Long userId, SseEmitter emitter) {
        synchronized (this) {
            if (currentConnections.get() >= MAX_CONNECTIONS) {
                log.warn("Max connections limit reached. Unable to connect user [{}].", userId);
                throw new CustomException(MAX_CONNECTIONS_EXCEEDED_ERROR);
            }
            incrementConnection();
            log.info("Connection incremented for user [{}]. Current connections: {}", userId, currentConnections.get());
        }

        try {
            establishConnection(userId, emitter);
            sendConnectionCompletionEvent(userId, emitter);
        } catch (Exception e) {
            decrementConnection();
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
            decrementConnection();
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection for user [{}] has timed out.", userId);
            releaseExternalResources(userId);
            decrementConnection();
        });

        emitter.onError((throwable) -> {
            log.error("Error with SSE connection for user [{}]: {}", userId, throwable.getMessage());
            releaseExternalResources(userId);
            decrementConnection();
        });

        // 연결이 끊어진 경우도 처리해줘야함
    }

    private void releaseExternalResources(Long userId) {
        sseEmitterRepository.delete(userId);
        log.info("External resources released and SSE emitter removed from the repository for user [{}].", userId);
    }

    private void sendConnectionCompletionEvent(Long userId, SseEmitter emitter) throws IOException {
        log.info("Attempting to send connection completion event for user {}.", userId);
        String eventID = String.valueOf(System.currentTimeMillis());
        emitter.send(SseEmitter.event()
                .id(eventID)
                .name(EVENT_NAME)
                .data("Connection completed"));
        log.info("Connection completion event for user {} has been sent successfully.", userId);
    }

    private void incrementConnection() {
        currentConnections.incrementAndGet();
    }

    private void decrementConnection() {
        currentConnections.decrementAndGet();
    }
}
