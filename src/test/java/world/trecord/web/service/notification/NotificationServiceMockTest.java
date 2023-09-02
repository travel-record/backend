package world.trecord.web.service.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceMockTest {

    @Mock
    SseEmitterRepository sseEmitterRepository;

    @Mock
    SseEmitterService sseEmitterService;

    @InjectMocks
    NotificationService notificationService;

    @Test
    @DisplayName("SseEmitter를 생성할 때 이벤트를 전송한다")
    void connectNotificationTest() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);
        Mockito.when(sseEmitterService.createSseEmitter(any())).thenReturn(mockEmitter);

        //when
        notificationService.connectNotification(userId, Duration.ofDays(1));

        //then
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }
}