package world.trecord.web.controller.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.*;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;
import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;

@MockMvcTestSupport
class NotificationControllerTest extends ContainerBaseTest {

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
    @DisplayName("GET /api/v1/notifications - 성공")
    void getNotificationsTest() throws Exception {
        //given
        UserEntity author = UserEntity.builder().email("test@email.com").build();
        UserEntity commenter1 = UserEntity.builder().nickname("nickname1").email("test1@email.com").build();
        UserEntity commenter2 = UserEntity.builder().nickname("nickname2").email("test2@email.com").build();
        UserEntity commenter3 = UserEntity.builder().nickname("nickname3").email("test3@email.com").build();

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
                                .header("Authorization", createToken(author.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.size()").value(3))
                .andExpect(jsonPath("$.data.notifications[0].content").value(notificationEntity3.getNotificationContent()))
                .andExpect(jsonPath("$.data.notifications[0].date", matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{type} - 성공")
    void getNotificationsByTypeTest() throws Exception {
        //given
        UserEntity author = createUser();
        UserEntity viewer1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity viewer2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity viewer3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());

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
                        get("/api/v1/notifications/{type}", RECORD_LIKE)
                                .header("Authorization", createToken(author.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.size()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{type} - 실패(존재하지 않는 타입)")
    void getNotificationsByNotExistingTypeTest() throws Exception {
        //given
        UserEntity author = createUser();

        String notExistingType = "NOT_EXISTING_TYPE";

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/{type}", notExistingType)
                                .header("Authorization", createToken(author.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/subscribe - 성공 (SseEmitter 반환)")
    void connectNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

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

    private UserEntity createUser() {
        return userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());
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