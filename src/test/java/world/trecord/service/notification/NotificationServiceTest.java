package world.trecord.service.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.notification.response.CheckNewNotificationResponse;
import world.trecord.dto.notification.response.NotificationListResponse;
import world.trecord.event.sse.SseEmitterRepository;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.comment.CommentService;
import world.trecord.service.record.RecordService;
import world.trecord.service.userrecordlike.UserRecordLikeService;

import java.time.LocalDateTime;
import java.util.List;

import static world.trecord.domain.notification.enumeration.NotificationStatus.READ;
import static world.trecord.domain.notification.enumeration.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;
import static world.trecord.domain.notification.enumeration.NotificationType.RECORD_LIKE;

@Transactional
@IntegrationTestSupport
class NotificationServiceTest extends AbstractContainerBaseTest {

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

    @Autowired
    SseEmitterRepository sseEmitterRepository;


    @Test
    @DisplayName("사용자가 알림 리스트를 조회하면 시간 내림차순으로 정렬된 알림 리스트를 반환한다")
    void getNotificationsByTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        UserEntity commenter1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity commenter2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity commenter3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity, "content2");
        CommentEntity commentEntity3 = createComment(commenter3, recordEntity, "content3");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        NotificationEntity notificationEntity1 = createNotification(author, commenter1, recordEntity, commentEntity1, READ, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, commenter2, recordEntity, commentEntity2, READ, RECORD_LIKE);
        NotificationEntity notificationEntity3 = createNotification(author, commenter3, recordEntity, commentEntity3, READ, COMMENT);
        NotificationEntity notificationEntity4 = createNotification(author, commenter1, recordEntity, commentEntity3, READ, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when
        NotificationListResponse response = notificationService.getNotifications(author.getId());

        //then
        Assertions.assertThat(response.getNotifications()).hasSize(4);
    }

    @Test
    @DisplayName("사용자가 알림 리스트를 조회하면 읽지 않은 알림을 모두 알림 처리 한다")
    void getNotificationsByTestUpdateUnreadToReadStatus() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        UserEntity commenter1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity commenter2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity commenter3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity, "content2");
        CommentEntity commentEntity3 = createComment(commenter3, recordEntity, "content3");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        NotificationEntity notificationEntity1 = createNotification(author, commenter1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, commenter2, recordEntity, commentEntity2, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity3 = createNotification(author, commenter3, recordEntity, commentEntity3, UNREAD, COMMENT);
        NotificationEntity notificationEntity4 = createNotification(author, commenter1, recordEntity, commentEntity3, UNREAD, RECORD_LIKE);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when
        notificationService.getNotifications(author.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .extracting("status")
                .containsOnly(READ);
    }

    @Test
    @DisplayName("알림 엔티티를 저장한 후 반환한다")
    void createNotificationTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        NotificationArgs args = NotificationArgs.builder()
                .build();

        //when
        notificationService.createNotification(userEntity.getId(), COMMENT, args);

        //then
        Assertions.assertThat(notificationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("알림 리스트가 없으면 알림 리스트로 조회 시 빈 배열을 반환한다")
    void getNotificationsByWithEmptyNotificationListTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        NotificationListResponse response = notificationService.getNotifications(userEntity.getId());

        //then
        Assertions.assertThat(response.getNotifications()).isEmpty();
    }

    @Test
    @DisplayName("사용자에게 읽지 않음 알림이 있으면 새로운 알림이 있음을 반환한다")
    void checkNewUnreadNotificationReturnTrueTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity commentEntity = createComment(userEntity, recordEntity, "content1");

        notificationRepository.save(createNotification(userEntity, null, recordEntity, commentEntity, UNREAD, COMMENT));

        //when
        CheckNewNotificationResponse response = notificationService.checkUnreadNotifications(userEntity.getId());

        //then
        Assertions.assertThat(response.isHasNewNotification()).isTrue();
    }

    @Test
    @DisplayName("사용자에게 읽지 않음 알림이 없으면 새로운 알림이 없음을 반환한다")
    void checkNewUnreadNotificationReturnFalseTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity commentEntity = createComment(userEntity, recordEntity, "content1");

        notificationRepository.save(createNotification(userEntity, null, recordEntity, commentEntity, READ, COMMENT));

        //when
        CheckNewNotificationResponse response = notificationService.checkUnreadNotifications(userEntity.getId());

        //then
        Assertions.assertThat(response.isHasNewNotification()).isFalse();
    }


    @Test
    @DisplayName("알림 타입 별로 알림 리스트를 등록 시간 내림차순으로 조회하여 반환한다")
    void getNotificationsByTypeTest() throws Exception {
        UserEntity author = createUser("test@email.com");
        UserEntity viewer1 = UserEntity.builder().nickname("nickname1").email("test1@email.com").build();
        UserEntity viewer2 = UserEntity.builder().nickname("nickname2").email("test2@email.com").build();
        UserEntity viewer3 = UserEntity.builder().nickname("nickname3").email("test3@email.com").build();
        userRepository.saveAll(List.of(author, viewer1, viewer2, viewer3));

        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity1 = createComment(viewer1, recordEntity, "content1");
        CommentEntity commentEntity2 = createComment(viewer2, recordEntity, "content2");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotification(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = createNotification(author, viewer3, recordEntity, null, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity4 = createNotification(author, viewer1, recordEntity, null, UNREAD, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        //when
        NotificationListResponse response = notificationService.getNotificationsByType(author.getId(), RECORD_LIKE);

        //then
        Assertions.assertThat(response.notifications).hasSize(2);
    }

    @Test
    @DisplayName("알림 리스트에서 알림 타입에 해당하는 알림이 없을때 Reponse에 빈 배열로 반환한다")
    void getNotificationsByTypeWhenTypeIsEmptyTest() throws Exception {
        //given
        UserEntity author = createUser("test@email.com");
        UserEntity viewer1 = UserEntity.builder().nickname("nickname1").email("test1@email.com").build();
        UserEntity viewer2 = UserEntity.builder().nickname("nickname2").email("test2@email.com").build();

        userRepository.saveAll(List.of(author, viewer1, viewer2));

        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity1 = createComment(viewer1, recordEntity, "content1");
        CommentEntity commentEntity2 = createComment(viewer2, recordEntity, "content2");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotification(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2));

        //when
        NotificationListResponse response = notificationService.getNotificationsByType(author.getId(), RECORD_LIKE);

        //then
        Assertions.assertThat(response.getNotifications()).isEmpty();
    }

    @Test
    @DisplayName("삭제된 기록에 대한 알림 리스트는 조회되지 않는다")
    void getNotificationsWhenRecordSoftDeletedTest() throws Exception {
        //given
        UserEntity author = createUser("test@email.com");
        UserEntity viewer1 = UserEntity.builder().nickname("nickname1").email("test1@email.com").build();
        UserEntity viewer2 = UserEntity.builder().nickname("nickname2").email("test2@email.com").build();
        userRepository.saveAll(List.of(author, viewer1, viewer2));

        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity1 = createComment(viewer1, recordEntity1, "content1");
        CommentEntity commentEntity2 = createComment(viewer2, recordEntity2, "content2");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = createNotification(author, viewer1, recordEntity1, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = createNotification(author, viewer2, recordEntity2, commentEntity2, UNREAD, COMMENT);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2));

        recordService.deleteRecord(author.getId(), recordEntity2.getId());

        //when
        NotificationListResponse response = notificationService.getNotifications(author.getId());

        //then
        Assertions.assertThat(response.getNotifications()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 아이디와 알림 아이디로 알림을 조회한 뒤, 알림을 soft delete 한다")
    void deleteNotificationTest() throws Exception {
        //given
        UserEntity user = userRepository.save(createUser("test@email.com"));
        NotificationEntity notificationEntity = notificationRepository.save(createNotification(user, null, null, null, UNREAD, COMMENT));

        //when
        notificationService.deleteNotification(user.getId(), notificationEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자 아이디와 알림 아이디로 알림이 조회되지 않으면 예외가 발생한다")
    void deleteNotificationWhenNotificationNotFoundTest() throws Exception {
        //given
        UserEntity user = userRepository.save(createUser("test@email.com"));
        long notExistingNotificationId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> notificationService.deleteNotification(user.getId(), notExistingNotificationId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOTIFICATION_NOT_FOUND);
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2022, 3, 2, 0, 0))
                .endAt(LocalDateTime.of(2022, 3, 10, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 3, 2, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

    private NotificationEntity createNotification(UserEntity userToEntity, UserEntity userFromEntity, RecordEntity recordEntity, CommentEntity commentEntity, NotificationStatus notificationStatus, NotificationType notificationType) {
        NotificationArgs args = NotificationArgs.builder()
                .commentEntity(commentEntity)
                .recordEntity(recordEntity)
                .userFromEntity(userFromEntity)
                .build();

        return NotificationEntity.builder()
                .usersToEntity(userToEntity)
                .type(notificationType)
                .status(notificationStatus)
                .args(args)
                .build();
    }
}