package world.trecord.service.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.notification.response.CheckNewNotificationResponse;
import world.trecord.dto.notification.response.NotificationResponse;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.fixture.*;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;

import static world.trecord.domain.notification.enumeration.NotificationStatus.READ;
import static world.trecord.domain.notification.enumeration.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;
import static world.trecord.domain.notification.enumeration.NotificationType.RECORD_LIKE;

@Transactional
class NotificationServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자가 알림 리스트를 조회하면 시간 내림차순으로 정렬된 알림 리스트를 페이지네이션으로 반환한다")
    void getNotificationsByTest() throws Exception {
        //given
        UserEntity author = UserEntityFixture.of("test@email.com");
        UserEntity commenter1 = UserEntityFixture.of("test1@email.com", "nickname1");
        UserEntity commenter2 = UserEntityFixture.of("test2@email.com", "nickname2");
        UserEntity commenter3 = UserEntityFixture.of("test3@email.com", "nickname3");
        userRepository.saveAll(List.of(author, commenter1, commenter2, commenter3));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        CommentEntity commentEntity1 = CommentEntityFixture.of(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = CommentEntityFixture.of(commenter2, recordEntity, "content2");
        CommentEntity commentEntity3 = CommentEntityFixture.of(commenter3, recordEntity, "content3");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        NotificationEntity notificationEntity1 = NotificationEntityFixture.of(author, commenter1, recordEntity, commentEntity1, READ, COMMENT);
        NotificationEntity notificationEntity2 = NotificationEntityFixture.of(author, commenter2, recordEntity, commentEntity2, READ, RECORD_LIKE);
        NotificationEntity notificationEntity3 = NotificationEntityFixture.of(author, commenter3, recordEntity, commentEntity3, READ, COMMENT);
        NotificationEntity notificationEntity4 = NotificationEntityFixture.of(author, commenter1, recordEntity, commentEntity3, READ, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationResponse> notifications = notificationService.getNotifications(author.getId(), pageRequest);

        //then
        Assertions.assertThat(notifications.getContent()).hasSize(pageSize);
        Assertions.assertThat(notifications.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(notifications.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("사용자가 알림 리스트를 조회하면 읽지 않은 알림을 모두 알림 처리 한다")
    void getNotificationsByTestUpdateUnreadToReadStatus() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity commenter1 = userRepository.save(UserEntity.builder().nickname("nickname1").email("test1@email.com").build());
        UserEntity commenter2 = userRepository.save(UserEntity.builder().nickname("nickname2").email("test2@email.com").build());
        UserEntity commenter3 = userRepository.save(UserEntity.builder().nickname("nickname3").email("test3@email.com").build());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        CommentEntity commentEntity1 = CommentEntityFixture.of(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = CommentEntityFixture.of(commenter2, recordEntity, "content2");
        CommentEntity commentEntity3 = CommentEntityFixture.of(commenter3, recordEntity, "content3");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        NotificationEntity notificationEntity1 = NotificationEntityFixture.of(author, commenter1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = NotificationEntityFixture.of(author, commenter2, recordEntity, commentEntity2, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity3 = NotificationEntityFixture.of(author, commenter3, recordEntity, commentEntity3, UNREAD, COMMENT);
        NotificationEntity notificationEntity4 = NotificationEntityFixture.of(author, commenter1, recordEntity, commentEntity3, UNREAD, RECORD_LIKE);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        notificationService.getNotifications(author.getId(), pageRequest);

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .extracting("status")
                .containsOnly(READ);
    }

    @Test
    @DisplayName("알림 엔티티를 저장한 후 반환한다")
    void createNotification() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        NotificationEntity notificationEntity = NotificationEntityFixture.of(userEntity, UNREAD);

        //when

        notificationService.createNotification(userEntity.getId(), notificationEntity.getType(), notificationEntity.getArgs());

        //then
        Assertions.assertThat(notificationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("알림 리스트가 없으면 알림 리스트로 조회 시 빈 배열을 반환한다")
    void getNotificationsByWithEmptyNotificationListTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationResponse> response = notificationService.getNotifications(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("사용자에게 읽지 않음 알림이 있으면 새로운 알림이 있음을 반환한다")
    void checkNewUnreadNotificationReturnTrueTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = CommentEntityFixture.of(userEntity, recordEntity, "content1");

        notificationRepository.save(NotificationEntityFixture.of(userEntity, null, recordEntity, commentEntity, UNREAD, COMMENT));

        //when
        CheckNewNotificationResponse response = notificationService.checkUnreadNotifications(userEntity.getId());

        //then
        Assertions.assertThat(response.isHasNewNotification()).isTrue();
    }

    @Test
    @DisplayName("사용자에게 읽지 않음 알림이 없으면 새로운 알림이 없음을 반환한다")
    void checkNewUnreadNotificationReturnFalseTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = CommentEntityFixture.of(userEntity, recordEntity, "content1");

        notificationRepository.save(NotificationEntityFixture.of(userEntity, null, recordEntity, commentEntity, READ, COMMENT));

        //when
        CheckNewNotificationResponse response = notificationService.checkUnreadNotifications(userEntity.getId());

        //then
        Assertions.assertThat(response.isHasNewNotification()).isFalse();
    }


    @Test
    @DisplayName("알림 타입 별로 알림 리스트를 등록 시간 내림차순으로 조회하여 반환한다")
    void getNotificationsByTypeTest() throws Exception {
        UserEntity author = UserEntityFixture.of("test@email.com");
        UserEntity viewer1 = UserEntityFixture.of();
        UserEntity viewer2 = UserEntityFixture.of();
        UserEntity viewer3 = UserEntityFixture.of();
        userRepository.saveAll(List.of(author, viewer1, viewer2, viewer3));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        CommentEntity commentEntity1 = CommentEntityFixture.of(viewer1, recordEntity, "content1");
        CommentEntity commentEntity2 = CommentEntityFixture.of(viewer2, recordEntity, "content2");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = NotificationEntityFixture.of(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = NotificationEntityFixture.of(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        NotificationEntity notificationEntity3 = NotificationEntityFixture.of(author, viewer3, recordEntity, null, UNREAD, RECORD_LIKE);
        NotificationEntity notificationEntity4 = NotificationEntityFixture.of(author, viewer1, recordEntity, null, UNREAD, RECORD_LIKE);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3, notificationEntity4));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationResponse> response = notificationService.getNotificationsByType(author.getId(), RECORD_LIKE, pageRequest);

        //then
        Assertions.assertThat(response.getContent()).hasSize(pageSize);
    }

    @Test
    @DisplayName("알림 리스트에서 알림 타입에 해당하는 알림이 없을때 Reponse에 빈 배열로 반환한다")
    void getNotificationsByTypeWhenTypeIsEmptyTest() throws Exception {
        //given
        UserEntity author = UserEntityFixture.of();
        UserEntity viewer1 = UserEntityFixture.of();
        UserEntity viewer2 = UserEntityFixture.of();

        userRepository.saveAll(List.of(author, viewer1, viewer2));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        CommentEntity commentEntity1 = CommentEntityFixture.of(viewer1, recordEntity, "content1");
        CommentEntity commentEntity2 = CommentEntityFixture.of(viewer2, recordEntity, "content2");
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        NotificationEntity notificationEntity1 = NotificationEntityFixture.of(author, viewer1, recordEntity, commentEntity1, UNREAD, COMMENT);
        NotificationEntity notificationEntity2 = NotificationEntityFixture.of(author, viewer2, recordEntity, commentEntity2, UNREAD, COMMENT);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationResponse> response = notificationService.getNotificationsByType(author.getId(), RECORD_LIKE, pageRequest);

        //then
        Assertions.assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("사용자 아이디와 알림 아이디로 알림을 조회한 뒤, 알림을 soft delete 한다")
    void deleteNotificationTest() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of());
        NotificationEntity notificationEntity = notificationRepository.save(NotificationEntityFixture.of(user, null, null, null, UNREAD, COMMENT));

        //when
        notificationService.deleteNotification(user.getId(), notificationEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자 아이디와 알림 아이디로 알림이 조회되지 않으면 예외가 발생한다")
    void deleteNotificationWhenNotificationNotFoundTest() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of("test@email.com"));
        long notExistingNotificationId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> notificationService.deleteNotification(user.getId(), notExistingNotificationId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOTIFICATION_NOT_FOUND);
    }

}