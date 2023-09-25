package world.trecord.event.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import world.trecord.event.sse.SseEmitterService;
import world.trecord.infra.test.AbstractMockTest;
import world.trecord.service.notification.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NotificationEventListenerTest extends AbstractMockTest {

    @InjectMocks
    NotificationEventListener notificationEventListener;

    @Mock
    NotificationService notificationService;

    @Mock
    SseEmitterService sseEmitterService;

    @Test
    @DisplayName("userToId와 userFromId가 동일하면 이벤트를 전송하지 않는다")
    void handleNotificationEventListener_whenUserToIdAndUserFromIdEqual_doNotSendEvent() throws Exception {
        //given
        Long userToId = 1L;
        Long userFromId = 1L;

        NotificationEvent notificationEvent = new NotificationEvent(userToId, userFromId, null, null);

        //when
        notificationEventListener.handleNotificationEventListener(notificationEvent);

        //then
        verify(notificationService, never()).createNotification(any(), any(), any());
    }
}