package world.trecord.service.sse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.RollbackIntegrationTestSupport;

import java.time.Duration;

@RollbackIntegrationTestSupport
class SseEmitterServiceTest extends ContainerBaseTest {

    @Autowired
    SseEmitterRepository sseEmitterRepository;

    @Autowired
    SseEmitterService sseEmitterService;

    @Test
    @DisplayName("userId로 SseEmitter를 repository에 저장한다")
    void connectNotificationTest() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter sseEmitter = new SseEmitter(Duration.ofDays(1).toMillis());

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