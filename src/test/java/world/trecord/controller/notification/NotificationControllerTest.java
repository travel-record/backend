package world.trecord.controller.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.security.JwtTokenHandler;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.*;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;
import static world.trecord.exception.CustomExceptionError.INVALID_ARGUMENT;
import static world.trecord.exception.CustomExceptionError.INVALID_TOKEN;

@Transactional
@MockMvcTestSupport
class NotificationControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenHandler jwtTokenHandler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProperties jwtProperties;

    @Test
    @DisplayName("GET /api/v1/notifications/check - 성공")
    void checkExistingNewNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com", "nickname"));

        NotificationEntity notificationEntity = createNotification(userEntity, UNREAD);

        notificationRepository.save(notificationEntity);

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/check")
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNewNotification").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/check - 성공 (새로운 알림이 없을 때)")
    void checkNotExistingNewNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com", "nickname"));

        NotificationEntity notificationEntity = createNotification(userEntity, READ);

        notificationRepository.save(notificationEntity);

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/check")
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNewNotification").value(false));
    }


    @Test
    @DisplayName("GET /api/v1/notifications - 성공")
    void getNotificationsTest() throws Exception {
        //given
        UserEntity author = createUser("test@email.com", "nickname");
        UserEntity commenter1 = createUser("test1@email.com", "nickname1");
        UserEntity commenter2 = createUser("test2@email.com", "nickname2");
        UserEntity commenter3 = createUser("test3@email.com", "nickname3");
        userRepository.saveAll(List.of(author, commenter1, commenter2, commenter3));

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author));

        RecordEntity recordEntity1 = createRecordEntity(feedEntity);
        RecordEntity recordEntity2 = createRecordEntity(feedEntity);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity1);
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity2);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotification(author, commenter1, recordEntity1, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, commenter2, recordEntity2, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = createNotification(author, commenter3, recordEntity2, null, UNREAD, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //whe //then
        mockMvc.perform(
                        get("/api/v1/notifications")
                                .header(AUTHORIZATION, createToken(author.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.size()").value(3))
                .andExpect(jsonPath("$.data.notifications[0].content").value(notificationEntity3.getNotificationContent()))
                .andExpect(jsonPath("$.data.notifications[0].date", matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - 실패 (토큰 없이)")
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
                                .header(AUTHORIZATION, createToken(invalidToken))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{type} - 성공")
    void getNotificationsByTypeTest() throws Exception {
        //given
        UserEntity author = createUser("test@email.com", "nickname");
        UserEntity viewer1 = createUser("test1@email.com", "nickname1");
        UserEntity viewer2 = createUser("test2@email.com", "nickname2");
        UserEntity viewer3 = createUser("test3@email.com", "nickname3");
        userRepository.saveAll(List.of(author, viewer1, viewer2, viewer3));

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity));

        CommentEntity commentEntity1 = createComment(viewer1, recordEntity);
        CommentEntity commentEntity2 = createComment(viewer2, recordEntity);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotification(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = createNotification(author, viewer3, recordEntity, null, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity4 = createNotification(author, viewer1, recordEntity, null, UNREAD, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/type/{type}", RECORD_LIKE)
                                .header(AUTHORIZATION, createToken(author.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.size()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{type} - 실패(존재하지 않는 타입)")
    void getNotificationsByNotExistingTypeTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com", "nickname"));
        String notExistingType = "NOT_EXISTING_TYPE";

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/type/{type}", notExistingType)
                                .header(AUTHORIZATION, createToken(author.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/subscribe - 성공 (SseEmitter 반환)")
    void connectNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com", "nickname"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/subscribe")
                                .queryParam("token", createToken(userEntity.getId()))
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com", "nickname"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/subscribe")
                                .header(ACCEPT, APPLICATION_JSON)
                                .queryParam("token", createToken(userEntity.getId()))
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

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }

    private UserEntity createUser(String email, String nickname) {
        return UserEntity.builder()
                .email(email)
                .nickname(nickname).build();
    }

    private FeedEntity createFeedEntity(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2022, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("title")
                .place("place")
                .date(LocalDateTime.of(2022, 3, 2, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content("content")
                .build();
    }

    private NotificationEntity createNotification(UserEntity userEntity, NotificationStatus notificationStatus) {
        return NotificationEntity.builder()
                .usersToEntity(userEntity)
                .type(COMMENT)
                .status(notificationStatus)
                .build();
    }

    private NotificationEntity createNotification(UserEntity userToEntity, UserEntity userFromEntity, RecordEntity recordEntity, CommentEntity commentEntity, NotificationStatus status, NotificationType type) {
        NotificationArgs args = NotificationArgs.builder()
                .commentEntity(commentEntity)
                .recordEntity(recordEntity)
                .userFromEntity(userFromEntity)
                .build();

        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .type(type)
                .status(status)
                .args(args)
                .build();
    }

}