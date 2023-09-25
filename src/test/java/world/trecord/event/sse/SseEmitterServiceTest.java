package world.trecord.event.sse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.time.Duration;

class SseEmitterServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("userId로 SseEmitter를 repository에 저장한다")
    void connectNotificationTest() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter sseEmitter = new SseEmitter(Duration.ofHours(3).toMillis());

        //when
        SseEmitter emitter = sseEmitterService.connect(userId, sseEmitter);

        //then
        Assertions.assertThat(sseEmitterRepository.findByUserId(userId))
                .isPresent()
                .hasValueSatisfying(it -> {
                    Assertions.assertThat(it).isEqualTo(emitter);
                });
    }
}