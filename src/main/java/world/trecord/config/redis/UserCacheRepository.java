package world.trecord.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import world.trecord.config.security.account.UserContext;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserCacheRepository {

    private final RedisTemplate<String, UserContext> userContextRedisTemplate;
    private static final Duration USER_CACHE_TTL = Duration.ofDays(1);

    public UserContext setUserContext(UserContext userContext) {
        String key = getKey(userContext.getId());
        log.info("Set UserContext to Redis {}({})", key, userContext);
        userContextRedisTemplate.opsForValue().set(key, userContext, USER_CACHE_TTL);
        return userContext;
    }

    public Optional<UserContext> getUserContext(Long userId) {
        String key = getKey(userId);
        UserContext userContext = userContextRedisTemplate.opsForValue().get(key);
        log.info("Get UserContext from Redis {}", userContext);
        return Optional.ofNullable(userContext);
    }

    private String getKey(Long userId) {
        return "UID:" + userId;
    }
}
