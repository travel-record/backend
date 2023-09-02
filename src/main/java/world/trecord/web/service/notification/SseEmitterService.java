package world.trecord.web.service.notification;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;

@Component
public class SseEmitterService {

    public SseEmitter createSseEmitter(Duration duration) {
        return new SseEmitter(duration.toMillis());
    }
}
