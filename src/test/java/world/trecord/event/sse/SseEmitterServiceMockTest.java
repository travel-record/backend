package world.trecord.event.sse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.exception.CustomException;
import world.trecord.infra.test.AbstractMockTest;
import world.trecord.service.notification.NotificationService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static world.trecord.exception.CustomExceptionError.MAX_CONNECTIONS_EXCEEDED_ERROR;
import static world.trecord.exception.CustomExceptionError.NOTIFICATION_CONNECT_ERROR;

class SseEmitterServiceMockTest extends AbstractMockTest {

    @Mock
    SseEmitterRepository sseEmitterRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    SseEmitterService sseEmitterService;

    @Test
    @DisplayName("sse connection을 성공하면 이벤트를 전송한다")
    void connectNotificationTest() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter mockSseEmitter = mock(SseEmitter.class);

        //when
        sseEmitterService.connect(userId, mockSseEmitter);

        //then
        verify(sseEmitterRepository).save(userId, mockSseEmitter);
        verify(mockSseEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("사용자에게 연결된 emitter로 이벤트를 전송한다")
    void sendWhenUserNotSameTest() throws Exception {
        //given
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(sseEmitterRepository.findByUserId(anyLong())).thenReturn(Optional.of(mockEmitter));
        SseEmitterEvent mockEvent = mock(SseEmitterEvent.class);

        //when
        sseEmitterService.send(0L, 1L, mockEvent);

        //then
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("MAX_CONNECTIONS를 초과할 때 예외 발생가 발생한다")
    void exceedMaxConnectionsTest() throws Exception {
        // given
        ReflectionTestUtils.setField(sseEmitterService, "currentConnections", new AtomicInteger(SseEmitterService.MAX_CONNECTIONS));

        // when //then
        Assertions.assertThatThrownBy(() -> sseEmitterService.connect((long) SseEmitterService.MAX_CONNECTIONS, new SseEmitter()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(MAX_CONNECTIONS_EXCEEDED_ERROR);
    }

    @Test
    @DisplayName("SseEmitter 전송 중 IOException 발생 시 예외를 처리한다")
    void handleIOExceptionDuringEmitterSendTest() throws Exception {
        // given
        Long userToId = 1L;
        Long userFromId = 2L;

        SseEmitter mockEmitter = mock(SseEmitter.class);
        SseEmitterEvent mockSseEmitterEvent = mock(SseEmitterEvent.class);

        when(sseEmitterRepository.findByUserId(userToId)).thenReturn(Optional.ofNullable(mockEmitter));
        doNothing().when(sseEmitterRepository).delete(any());
        doThrow(new IOException("Test exception")).when(mockEmitter).send(any());

        // then
        Assertions.assertThatThrownBy(() -> sseEmitterService.send(userToId, userFromId, mockSseEmitterEvent))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("SseEmitter 연결 중 예외 발생 시 연결 수를 감소시킨다")
    void handleExceptionDuringEmitterConnectTest() throws Exception {
        // given
        Long userId = 1L;
        SseEmitter mockSseEmitter = mock(SseEmitter.class);

        AtomicInteger mockCurrentConnections = mock(AtomicInteger.class);
        Field currentConnectionsField = SseEmitterService.class.getDeclaredField("currentConnections");
        currentConnectionsField.setAccessible(true);
        currentConnectionsField.set(sseEmitterService, mockCurrentConnections);

        doThrow(new RuntimeException("Test exception")).when(sseEmitterRepository).save(anyLong(), any());

        // when
        Assertions.assertThatThrownBy(() -> sseEmitterService.connect(userId, mockSseEmitter))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOTIFICATION_CONNECT_ERROR);

        verify(mockCurrentConnections, times(1)).incrementAndGet();
        verify(mockCurrentConnections, times(1)).decrementAndGet();
    }
}