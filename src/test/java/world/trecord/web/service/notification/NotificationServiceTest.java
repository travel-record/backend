package world.trecord.web.service.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
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
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.service.comment.CommentService;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;
import world.trecord.web.service.record.RecordService;
import world.trecord.web.service.userrecordlike.UserRecordLikeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;

@IntegrationTestSupport
class NotificationServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    CommentService commentService;

    @Autowired
    RecordService recordService;

    @Autowired
    UserRecordLikeService userRecordLikeService;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Test
    @DisplayName("사용자가 기록에 댓글을 작성하면 댓글 기록 알림을 생성하여 반환한다")
    void createNotificationTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());
        UserEntity commenter = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = createCommentEntity(commenter, recordEntity, "content1");

        //when
        notificationService.createCommentNotification(commentEntity);

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .hasSize(1)
                .extracting("type", "status", "usersToEntity", "usersFromEntity", "commentEntity", "recordEntity")
                .containsExactly(
                        tuple(COMMENT, UNREAD, author, commenter, commentEntity, recordEntity)
                );
    }

    @Test
    @DisplayName("기록 작성자가 자신의 기록에 댓글을 작성하면 알림이 생성되지 않는다")
    void createNotificationItselfTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = createCommentEntity(author, recordEntity, "content1");

        //when
        notificationService.createCommentNotification(commentEntity);

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자에게 읽지 않음 알림이 있으면 새로운 알림이 있음을 반환한다")
    void checkNewUnreadNotificationReturnTrueTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = createCommentEntity(userEntity, recordEntity, "content1");

        NotificationEntity notificationEntity = createNotificationEntity(userEntity, null, recordEntity, commentEntity, UNREAD, COMMENT);

        notificationRepository.save(notificationEntity);

        //when
        CheckNewNotificationResponse response = notificationService.checkNewNotificationBy(userEntity.getId());

        //then
        Assertions.assertThat(response.isHasNewNotification()).isTrue();
    }

    @Test
    @DisplayName("사용자에게 읽지 않음 알림이 없으면 새로운 알림이 없음을 반환한다")
    void checkNewUnreadNotificationReturnFalseTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = createCommentEntity(userEntity, recordEntity, "content1");

        NotificationEntity notificationEntity = createNotificationEntity(userEntity, null, recordEntity, commentEntity, READ, COMMENT);

        notificationRepository.save(notificationEntity);

        //when
        CheckNewNotificationResponse response = notificationService.checkNewNotificationBy(userEntity.getId());

        //then
        Assertions.assertThat(response.isHasNewNotification()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 새로운 알림 체크 요청을 하면 예외가 발생한다")
    void checkNewNotificationWithUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> notificationService.checkNewNotificationBy(notExistingUserId))
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("사용자가 알림 리스트를 조회하면 시간 내림차순으로 정렬된 알림 리스트를 반환한다")
    void getNotificationsByTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity commenter2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity commenter3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");
        CommentEntity commentEntity3 = createCommentEntity(commenter3, recordEntity, "content3");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        NotificationEntity notificationEntity1 = createNotificationEntity(author, commenter1, recordEntity, commentEntity1, READ, COMMENT);
        NotificationEntity notificationEntity2 = createNotificationEntity(author, commenter2, recordEntity, commentEntity2, READ, RECORD_LIKE);
        NotificationEntity notificationEntity3 = createNotificationEntity(author, commenter3, recordEntity, commentEntity3, READ, COMMENT);
        NotificationEntity notificationEntity4 = createNotificationEntity(author, commenter1, recordEntity, commentEntity3, READ, RECORD_LIKE);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when
        NotificationListResponse response = notificationService.getNotifications(author.getId());

        //then
        Assertions.assertThat(response.getNotifications())
                .hasSize(4)
                .extracting("type", "senderId", "content")
                .containsExactly(
                        tuple(RECORD_LIKE, commenter1.getId(), notificationEntity4.getNotificationContent()),
                        tuple(COMMENT, commenter3.getId(), notificationEntity3.getNotificationContent()),
                        tuple(RECORD_LIKE, commenter2.getId(), notificationEntity2.getNotificationContent()),
                        tuple(COMMENT, commenter1.getId(), notificationEntity1.getNotificationContent())
                );
    }

    @Test
    @DisplayName("사용자가 알림 리스트를 조회하면 읽지 않은 알림을 모두 알림 처리 한다")
    void getNotificationsByTestUpdateUnreadToReadStatus() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity commenter2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity commenter3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");
        CommentEntity commentEntity3 = createCommentEntity(commenter3, recordEntity, "content3");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        NotificationEntity notificationEntity1 = createNotificationEntity(author, commenter1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotificationEntity(author, commenter2, recordEntity, commentEntity2, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity3 = createNotificationEntity(author, commenter3, recordEntity, commentEntity3, UNREAD, COMMENT);
        NotificationEntity notificationEntity4 = createNotificationEntity(author, commenter1, recordEntity, commentEntity3, UNREAD, RECORD_LIKE);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when
        notificationService.getNotifications(author.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .extracting("status")
                .containsOnly(READ);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 알림 리스트를 조회하면 예외가 발생한다")
    void getNotificationsByNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> notificationService.getNotifications(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("알림 리스트가 없으면 알림 리스트로 조회 시 빈 배열을 반환한다")
    void getNotificationsByWithEmptyNotificationListTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        //when
        NotificationListResponse response = notificationService.getNotifications(userEntity.getId());

        //then
        Assertions.assertThat(response.getNotifications()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 다른 사용자의 기록에 좋아요하면 좋아요 알림을 생성하여 반환한다 ")
    void createRecordLikeNotificationTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        UserEntity viewer = userRepository.save(UserEntity.builder().email("test2@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        notificationService.createRecordLikeNotification(recordEntity, viewer);

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .hasSize(1)
                .extracting("type", "status", "usersToEntity", "usersFromEntity", "recordEntity")
                .containsExactly(
                        tuple(RECORD_LIKE, UNREAD, writer, viewer, recordEntity)
                );
    }

    @Test
    @DisplayName("기록 작성자 본인이 자신의 기록에 좋아요하면 좋아요 알림이 생성되지 않는다")
    void createRecordLikeNotificationWhenAuthorLikeSelfTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        notificationService.createRecordLikeNotification(recordEntity, writer);

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("알림 타입 별로 알림 리스트를 등록 시간 내림차순으로 조회하여 반환한다")
    void getNotificationsByTypeTest() throws Exception {
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

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

        //when
        NotificationListResponse response = notificationService.getNotifications(author.getId(), RECORD_LIKE);

        //then
        Assertions.assertThat(response.notifications)
                .hasSize(2)
                .extracting("senderId", "type")
                .containsExactly(
                        tuple(viewer1.getId(), RECORD_LIKE),
                        tuple(viewer3.getId(), RECORD_LIKE)
                );
    }

    @Test
    @DisplayName("알림 리스트에서 알림 타입에 해당하는 알림이 없을때 Reponse에 빈 배열로 반환한다")
    void getNotificationsByTypeWhenTypeIsEmptyTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        UserEntity viewer1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity viewer2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(viewer1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(viewer2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotificationEntity(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotificationEntity(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2));

        //when
        NotificationListResponse response = notificationService.getNotifications(author.getId(), RECORD_LIKE);

        //then
        Assertions.assertThat(response.getNotifications()).isEmpty();
    }

    @Test
    @DisplayName("삭제된 기록에 대한 알림 리스트는 조회되지 않는다")
    void getNotificationsWhenRecordSoftDeletedTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());

        UserEntity viewer1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity viewer2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity1 = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        RecordEntity recordEntity2 = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(viewer1, recordEntity1, "content1");
        CommentEntity commentEntity2 = createCommentEntity(viewer2, recordEntity2, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotificationEntity(author, viewer1, recordEntity1, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotificationEntity(author, viewer2, recordEntity2, commentEntity2, UNREAD, COMMENT);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2));

        recordService.deleteRecord(author.getId(), recordEntity2.getId());

        //when
        NotificationListResponse response = notificationService.getNotifications(author.getId());

        //then
        Assertions.assertThat(response.getNotifications())
                .hasSize(1)
                .extracting("recordId")
                .containsOnly(recordEntity1.getId());
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

    private CommentEntity createCommentEntity(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
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

    private NotificationEntity createNotificationEntity(UserEntity userToEntity, UserEntity userFromEntity, RecordEntity recordEntity, CommentEntity commentEntity, NotificationStatus notificationStatus, NotificationType notificationType) {
        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .usersFromEntity(userFromEntity)
                .commentEntity(commentEntity)
                .recordEntity(recordEntity)
                .type(notificationType)
                .status(notificationStatus)
                .build();
    }
}