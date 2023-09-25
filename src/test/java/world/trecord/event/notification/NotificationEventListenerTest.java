package world.trecord.event.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.event.sse.SseEmitterEvent;
import world.trecord.event.sse.SseEmitterService;
import world.trecord.infra.test.AbstractMockTest;
import world.trecord.service.notification.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("userToId와 userFromId가 다르면 NotificationEntity를 생성하고 이벤트를 전송한다")
    void handleNotificationEventListener_whenUserToIdAndUserFromIdDiffer_sendEvent() {
        //given
        Long userToId = 1L;
        Long userFromId = 2L;
        NotificationType type = mock(NotificationType.class);
        NotificationArgs args = mock(NotificationArgs.class);

        NotificationEntity mockNotificationEntity = mock(NotificationEntity.class);
        when(mockNotificationEntity.getArgs()).thenReturn(args);
        when(mockNotificationEntity.getArgs().getFeed()).thenReturn(null);
        when(mockNotificationEntity.getArgs().getRecord()).thenReturn(null);
        when(mockNotificationEntity.getArgs().getComment()).thenReturn(null);
        when(mockNotificationEntity.getId()).thenReturn(123L);
        when(notificationService.createNotification(userToId, type, args)).thenReturn(mockNotificationEntity);

        NotificationEvent notificationEvent = new NotificationEvent(userToId, userFromId, type, args);

        //when
        notificationEventListener.handleNotificationEventListener(notificationEvent);

        //then
        verify(notificationService, times(1)).createNotification(userToId, type, args);
        verify(sseEmitterService, times(1)).send(any(), any(), any(SseEmitterEvent.class));
    }

    @Test
    @DisplayName("NotificationService에서 예외가 발생하면 이벤트를 전송하지 않는다")
    void handleNotificationEventListener_whenExceptionInService_doNotSendEvent() {
        //given
        Long userToId = 1L;
        Long userFromId = 2L;
        NotificationType type = mock(NotificationType.class);
        NotificationArgs args = mock(NotificationArgs.class);

        when(notificationService.createNotification(userToId, type, args))
                .thenThrow(new RuntimeException("Test exception"));

        NotificationEvent notificationEvent = new NotificationEvent(userToId, userFromId, type, args);

        //when
        Assertions.assertThatThrownBy(() -> notificationEventListener.handleNotificationEventListener(notificationEvent))
                .isInstanceOf(RuntimeException.class);
        
        verify(sseEmitterService, never()).send(anyLong(), anyLong(), any(SseEmitterEvent.class));
    }


}