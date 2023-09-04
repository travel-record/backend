package world.trecord.service.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.service.notification.NotificationService;
import world.trecord.service.sse.SseEmitterRepository;
import world.trecord.service.sse.SseEmitterService;

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

//    @Test
//    @DisplayName("MAX_CONNECTIONS를 초과할 때 예외 발생")
//    void exceedMaxConnectionsTest() throws Exception {
//        // given
//        for (int i = 0; i < SseEmitterService.MAX_CONNECTIONS; i++) {
//            sseEmitterService.connect((long) i, new SseEmitter());
//        }
//
//        // when
//        Throwable exception = assertThrows(CustomException.class, () -> {
//            sseEmitterService.connect((long) SseEmitterService.MAX_CONNECTIONS, new SseEmitter());
//        });
//
//        // then
//        assertEquals(SseEmitterService.MAX_CONNECTIONS_EXCEEDED_ERROR, exception.getMessage());
//
//        int currentConnections = (int) ReflectionTestUtils.getPrivateField(sseEmitterService, "currentConnections");
//        assertEquals(SseEmitterService.MAX_CONNECTIONS, currentConnections);
//    }
//
//    @Test
//    @DisplayName("SseEmitter 전송 중 IOException 발생 시 예외를 처리한다")
//    void handleIOExceptionDuringEmitterSendTest() throws Exception {
//        // given
//        Long userToId = 1L;
//        Long userFromId = 2L;
//
//        SseEmitter mockEmitter = mock(SseEmitter.class);
//        NotificationEntity mockNotification = mock(NotificationEntity.class);
//        NotificationArgs mockArgs = mock(NotificationArgs.class);
//
//        when(mockNotification.getId()).thenReturn(1L);
//        when(sseEmitterRepository.findByUserId(userToId)).thenReturn(Optional.of(mockEmitter));
//        when(notificationService.createNotification(eq(userToId), any(), any())).thenReturn(mockNotification);
//        doThrow(new IOException("Test exception")).when(mockEmitter).send(any());
//
//        // then
//        assertThrows(CustomException.class, () -> {
//            // when
//            sseEmitterService.send(userToId, userFromId, null, mockArgs);
//        });
//    }
//    @Test
//    @DisplayName("SseEmitter 연결 중 예외 발생 시 연결 수를 감소시킨다")
//    void handleExceptionDuringEmitterConnectTest() {
//        // given
//        Long userId = 1L;
//        SseEmitter mockSseEmitter = mock(SseEmitter.class);
//
//        doThrow(new RuntimeException("Test exception")).when(sseEmitterRepository).save(anyLong(), any());
//
//        try {
//            // when
//            sseEmitterService.connect(userId, mockSseEmitter);
//        } catch (Exception e) {
//            // then
//            assertEquals(currentConnections.get(), 0);  // 현재 연결 수는 0이어야 합니다.
//        }
//    }

}