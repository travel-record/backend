package world.trecord.controller.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.*;
import world.trecord.infra.support.WithTestUser;
import world.trecord.infra.test.AbstractMockMvcTest;

import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.domain.notification.enumeration.NotificationStatus.READ;
import static world.trecord.domain.notification.enumeration.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;
import static world.trecord.domain.notification.enumeration.NotificationType.RECORD_LIKE;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
class NotificationControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("GET /api/v1/notifications/check - 성공")
    @WithTestUser("user@email.com")
    void checkExistingNewNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        NotificationEntity notificationEntity = NotificationEntityFixture.of(userEntity, UNREAD);
        notificationRepository.save(notificationEntity);

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/check")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNewNotification").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/check - 성공 (새로운 알림이 없을 때)")
    @WithTestUser("user@email.com")
    void checkNotExistingNewNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        NotificationEntity notificationEntity = NotificationEntityFixture.of(userEntity, READ);
        notificationRepository.save(notificationEntity);

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/check")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNewNotification").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - 성공")
    @WithTestUser("user@email.com")
    void getNotificationsTest() throws Exception {
        //given
        UserEntity author = userRepository.findByEmail("user@email.com").get();
        UserEntity commenter1 = UserEntityFixture.of("test1@email.com", "nickname1");
        UserEntity commenter2 = UserEntityFixture.of("test2@email.com", "nickname2");
        UserEntity commenter3 = UserEntityFixture.of("test3@email.com", "nickname3");
        userRepository.saveAll(List.of(commenter1, commenter2, commenter3));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));

        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        CommentEntity commentEntity1 = CommentEntityFixture.of(commenter1, recordEntity1);
        CommentEntity commentEntity2 = CommentEntityFixture.of(commenter2, recordEntity2);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = NotificationEntityFixture.of(author, commenter1, recordEntity1, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = NotificationEntityFixture.of(author, commenter2, recordEntity2, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = NotificationEntityFixture.of(author, commenter3, recordEntity2, null, UNREAD, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //whe //then
        mockMvc.perform(
                        get("/api/v1/notifications")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - 실패 (토큰 없이)")
    @WithAnonymousUser
    void getNotificationsWithoutTokenTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - 실패 (올바르지 않은 토큰)")
    void getNotificationsWithInvalidTokenTest() throws Exception {
        //given
        long invalidToken = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/type/{type} - 성공")
    @WithTestUser("user@email.com")
    void getNotificationsByTypeTest() throws Exception {
        //given
        UserEntity author = userRepository.findByEmail("user@email.com").get();
        UserEntity viewer1 = UserEntityFixture.of("test1@email.com", "nickname1");
        UserEntity viewer2 = UserEntityFixture.of("test2@email.com", "nickname2");
        UserEntity viewer3 = UserEntityFixture.of("test3@email.com", "nickname3");
        userRepository.saveAll(List.of(viewer1, viewer2, viewer3));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));

        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        CommentEntity commentEntity1 = CommentEntityFixture.of(viewer1, recordEntity);
        CommentEntity commentEntity2 = CommentEntityFixture.of(viewer2, recordEntity);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = NotificationEntityFixture.of(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = NotificationEntityFixture.of(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = NotificationEntityFixture.of(author, viewer3, recordEntity, null, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity4 = NotificationEntityFixture.of(author, viewer1, recordEntity, null, UNREAD, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/type/{type}", RECORD_LIKE)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/type/{type} - 실패(존재하지 않는 타입)")
    @WithTestUser
    void getNotificationsByNotExistingTypeTest() throws Exception {
        //given
        String notExistingType = "NOT_EXISTING_TYPE";

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/type/{type}", notExistingType)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/subscribe - 성공 (SseEmitter 반환)")
    void connectNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/subscribe")
                                .queryParam("token", token(userEntity.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/notifications/subscribe - 실패 (인증 토큰 없이)")
    void connectNotificationWithoutTokenTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/subscribe")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/notifications/subscribe - 실패 (accept 다름)")
    void connectNotificationWithInvalidAcceptTypeTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com", "nickname"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/subscribe")
                                .header(ACCEPT, APPLICATION_JSON)
                                .queryParam("token", token(userEntity.getId()))
                )
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/subscribe - 실패 (유효하지 않은 토큰)")
    void connectNotificationWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/subscribe")
                                .queryParam("token", invalidToken)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{notificationId} - 성공")
    @WithTestUser("user@email.com")
    void deleteNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        NotificationEntity notificationEntity = notificationRepository.save(NotificationEntityFixture.of(userEntity, UNREAD));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/notifications/{notificationId}", notificationEntity.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{notificationId} - 실패 (존재하지 않는 알림)")
    @WithTestUser("user@email.com")
    void deleteNotificationWhenNotificationNotFoundTest() throws Exception {
        //given
        UserEntity author = userRepository.findByEmail("user@email.com").get();
        long notExistingNotificationId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/notifications/{notificationId}", notExistingNotificationId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(NOTIFICATION_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{notificationId} - 실패 (토큰 없이 요청한 경우)")
    @WithAnonymousUser
    void deleteNotificationWithoutTokenTest() throws Exception {
        //given
        long notExistingNotificationId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/notifications/{notificationId}", notExistingNotificationId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{notificationId} - 실패 (유효하지 않은 토큰으로 요청한 경우)")
    void deleteNotificationWithInvalidTokenTest() throws Exception {
        //given
        long notExistingNotificationId = 0L;
        String invalidToken = "invalid Token";

        //when//then
        mockMvc.perform(
                        delete("/api/v1/notifications/{notificationId}", notExistingNotificationId)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }
}