package world.trecord.service.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// TODO redis 사용
@Slf4j
@RequiredArgsConstructor
@Repository
public class SseEmitterRepository {

    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(Long userId, SseEmitter emitter) {
        final String key = getKey(userId);
        log.info("Set Emitter to EmitterMap {}:({})", key, emitter);
        emitterMap.put(key, emitter);
        return emitter;
    }

    public void delete(Long userId) {
        emitterMap.remove(getKey(userId));
    }

    public Optional<SseEmitter> findByUserId(Long userId) {
        String key = getKey(userId);
        log.info("Get Emitter from EmitterMap {}", key);
        return Optional.ofNullable(emitterMap.get(key));
    }

    private String getKey(Long userId) {
        return "emitter:UID:" + userId;
    }
}