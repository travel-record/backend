package world.trecord.event.sse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.infra.test.AbstractIntegrationTest;

class SseEmitterRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("유저 아이디를 key로 emitter를 저장한다")
    void saveTest() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter sseEmitter = new SseEmitter();

        //when
        sseEmitterRepository.save(userId, sseEmitter);

        //then
        Assertions.assertThat(sseEmitterRepository.findByUserId(userId)).isPresent();
    }

    @Test
    @DisplayName("저장된 emitter를 삭제한다")
    void deleteTest() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter sseEmitter = new SseEmitter();

        sseEmitterRepository.save(userId, sseEmitter);

        //when
        sseEmitterRepository.delete(userId);

        //then
        Assertions.assertThat(sseEmitterRepository.findByUserId(userId)).isEmpty();
    }

}