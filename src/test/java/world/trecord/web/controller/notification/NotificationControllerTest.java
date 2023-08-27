package world.trecord.web.controller.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.MockMvcTestSupport;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.notification.NotificationStatus;
import world.trecord.domain.notification.NotificationType;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.security.jwt.JwtTokenHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;
import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;

@MockMvcTestSupport
class NotificationControllerTest {

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

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    @Test
    @DisplayName("GET /api/v1/notifications/check - 성공")
    void checkExistingNewNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        NotificationEntity notificationEntity = createNotificationEntity(userEntity, UNREAD);

        notificationRepository.save(notificationEntity);

        String token = jwtTokenHandler.generateToken(userEntity.getId(), secretKey, expiredTimeMs);

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/check")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNewNotification").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/check - 새로운 알림이 없을때 성공")
    void checkNotExistingNewNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        NotificationEntity notificationEntity = createNotificationEntity(userEntity, READ);

        notificationRepository.save(notificationEntity);

        String token = jwtTokenHandler.generateToken(userEntity.getId(), secretKey, expiredTimeMs);

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/check")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNewNotification").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - 성공")
    void getNotificationsTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        String token = jwtTokenHandler.generateToken(author.getId(), secretKey, expiredTimeMs);

        UserEntity commenter1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity commenter2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity commenter3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity1 = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        RecordEntity recordEntity2 = recordRepository.save(createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity1, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity2, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotificationEntity(author, commenter1, recordEntity1, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotificationEntity(author, commenter2, recordEntity2, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = createNotificationEntity(author, commenter3, recordEntity2, null, UNREAD, RECORD_LIKE);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //whe //then
        mockMvc.perform(
                        get("/api/v1/notifications")
                                .header("Authorization", token)
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
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        String token = jwtTokenHandler.generateToken(author.getId(), secretKey, expiredTimeMs);

        UserEntity viewer1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity viewer2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity viewer3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(viewer1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(viewer2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotificationEntity(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotificationEntity(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = createNotificationEntity(author, viewer3, recordEntity, null, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity4 = createNotificationEntity(author, viewer1, recordEntity, null, UNREAD, RECORD_LIKE);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/{type}", RECORD_LIKE)
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications.size()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{type} - 실패(존재하지 않는 타입)")
    void getNotificationsByNotExistingTypeTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        String token = jwtTokenHandler.generateToken(author.getId(), secretKey, expiredTimeMs);

        String notExistingType = "NOT_EXISTING_TYPE";

        //when //then
        mockMvc.perform(
                        get("/api/v1/notifications/{type}", notExistingType)
                                .header("Authorization", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    private NotificationEntity createNotificationEntity(UserEntity userEntity, NotificationStatus notificationStatus) {
        return NotificationEntity.builder()
                .usersToEntity(userEntity)
                .type(COMMENT)
                .status(notificationStatus)
                .build();
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private CommentEntity createCommentEntity(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, String title, String place, LocalDateTime date, String content, String weather, String satisfaction, String feeling) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(title)
                .place(place)
                .date(date)
                .content(content)
                .weather(weather)
                .transportation(satisfaction)
                .feeling(feeling)
                .build();
    }

    private NotificationEntity createNotificationEntity(UserEntity userToEntity, UserEntity userFromEntity, RecordEntity recordEntity, CommentEntity commentEntity, NotificationStatus status, NotificationType type) {
        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .usersFromEntity(userFromEntity)
                .recordEntity(recordEntity)
                .commentEntity(commentEntity)
                .type(type)
                .status(status)
                .build();
    }

}