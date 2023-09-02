package world.trecord.web.service.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.web.service.notification.NotificationService;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseEmitterServiceMockTest {

    @Mock
    SseEmitterRepository sseEmitterRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    SseEmitterService sseEmitterService;

    @Test
    @DisplayName("sse connections을 성공하면 이벤트를 전송한다")
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
    @DisplayName("userToId와 userFromId가 동일하면 이벤트를 전송하지 않는다")
    void sendWhenUserSameTest() throws Exception {
        //given
        Long userToId = 1L;
        Long userFromId = 1L;

        //when
        sseEmitterService.send(userToId, userFromId, null, null);

        //then
        verify(notificationService, never()).createNotification(any(), any(), any());
    }

    @Test
    @DisplayName("사용자에게 연결된 emitter로 이벤트를 전송한다")
    void sendWhenUserNotSameTest() throws Exception {
        //given
        Long userToId = 1L;
        Long userFromId = 2L;

        SseEmitter mockEmitter = mock(SseEmitter.class);

        NotificationEntity mockNotification = mock(NotificationEntity.class);
        NotificationArgs mockArgs = mock(NotificationArgs.class);

        when(mockNotification.getId()).thenReturn(1L);
        when(mockNotification.getStatus()).thenReturn(null);
        when(mockNotification.getType()).thenReturn(null);
        when(mockNotification.getNotificationContent()).thenReturn(null);
        when(mockNotification.getCreatedDateTime()).thenReturn(null);

        when(mockNotification.getArgs()).thenReturn(mockArgs);

        when(mockArgs.getRecordId()).thenReturn(1L);
        when(mockArgs.getCommentId()).thenReturn(1L);
        when(mockArgs.getUserFromId()).thenReturn(userFromId);
        when(mockArgs.getUserFromNickname()).thenReturn("nickname");

        when(sseEmitterRepository.findByUserId(userToId)).thenReturn(Optional.of(mockEmitter));
        when(notificationService.createNotification(eq(userToId), any(), any())).thenReturn(mockNotification);

        //when
        sseEmitterService.send(userToId, userFromId, null, null);

        //then
        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("사용자에게 연결된 emitter가 없으면 이벤트를 전송하지 않는다")
    void sendWhenUserDifferentAndEmitterAbsentTest() {
        //given
        Long userToId = 1L;
        Long userFromId = 2L;

        NotificationEntity mockNotification = mock(NotificationEntity.class);
        when(notificationService.createNotification(eq(userToId), any(), any())).thenReturn(mockNotification);
        when(sseEmitterRepository.findByUserId(userToId)).thenReturn(Optional.empty());

        //when
        sseEmitterService.send(userToId, userFromId, null, null);

        //then
        verify(notificationService).createNotification(any(), any(), any());
    }
}