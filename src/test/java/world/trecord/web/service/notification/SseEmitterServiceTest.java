package world.trecord.web.service.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.Duration;

@IntegrationTestSupport
class SseEmitterServiceTest extends ContainerBaseTest {

    @Autowired
    SseEmitterRepository sseEmitterRepository;

    @Autowired
    SseEmitterService sseEmitterService;

    @Test
    @DisplayName("새로운 SseEmitter를 생성한다")
    void createSseEmitterTest() throws Exception {
        //given
        Duration duration = Duration.ofDays(1);

        //when
        SseEmitter emitter = sseEmitterService.createSseEmitter(duration);

        //then
        Assertions.assertThat(emitter.getTimeout()).isEqualTo(duration.toMillis());
    }
}