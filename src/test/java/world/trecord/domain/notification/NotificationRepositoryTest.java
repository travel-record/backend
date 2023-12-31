package world.trecord.domain.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;
import java.util.Optional;

import static world.trecord.domain.notification.enumeration.NotificationStatus.READ;
import static world.trecord.domain.notification.enumeration.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;
import static world.trecord.domain.notification.enumeration.NotificationType.RECORD_LIKE;

@Transactional
class NotificationRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자에게 읽지 않은 알림이 있으면 새로운 알림이 있음을 반환한다")
    void existsByUsersToEntityIdAndUnreadStatusTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        NotificationEntity notificationEntity = createNotification(userEntity, null, null, COMMENT, UNREAD);

        notificationRepository.save(notificationEntity);

        //when
        boolean result = notificationRepository.existsByUsersToEntityIdAndStatus(userEntity.getId(), UNREAD);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자에게 읽지 않은 알림이 있으면 새로운 알림이 없음을 반환한다")
    void existsByUsersToEntityIdAndReadStatusTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        NotificationEntity notificationEntity = createNotification(userEntity, null, null, COMMENT, READ);

        notificationRepository.save(notificationEntity);

        //when
        boolean result = notificationRepository.existsByUsersToEntityIdAndStatus(userEntity.getId(), UNREAD);

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자의 알림을 페이지네이션으로 조회한다")
    void findByUsersToEntityOrderByCreatedDateTimeDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        NotificationEntity notificationEntity1 = createNotification(userEntity, null, null, COMMENT, READ);
        NotificationEntity notificationEntity2 = createNotification(userEntity, null, null, COMMENT, READ);
        NotificationEntity notificationEntity3 = createNotification(userEntity, null, null, COMMENT, READ);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationEntity> entityPage = notificationRepository.findByUsersToEntityId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(entityPage.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("사용자 알림이 존재하지 않으면 빈 배열로 반환한다")
    void findByUsersToEntityOrderByCreatedDateTimeDescWhenNotificationsEmtpyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationEntity> entityPage = notificationRepository.findByUsersToEntityId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(entityPage.getContent()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 읽지 않은 알림을 모두 읽음 처리하여 처리된 개수를 반환한다")
    void updateNotificationStatusByUserIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        NotificationEntity notificationEntity1 = createNotification(userEntity, null, null, COMMENT, UNREAD);
        NotificationEntity notificationEntity2 = createNotification(userEntity, null, null, COMMENT, READ);
        NotificationEntity notificationEntity3 = createNotification(userEntity, null, null, COMMENT, UNREAD);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //when
        int count = notificationRepository.updateNotificationStatusByUserId(userEntity.getId(), UNREAD, READ);

        //then
        Assertions.assertThat(count).isEqualTo(2);
        Assertions.assertThat(notificationRepository.findAll())
                .filteredOn(notificationEntity -> notificationEntity.getStatus().equals(READ))
                .hasSize(3);
    }

    @Test
    @DisplayName("알림 타입별로 알림 리스트를 조회하여 반환한다")
    void findByUsersToEntityAndTypeOrderByCreatedDateTimeDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        NotificationEntity notificationEntity1 = createNotification(userEntity, null, null, COMMENT, UNREAD);
        NotificationEntity notificationEntity2 = createNotification(userEntity, null, null, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity3 = createNotification(userEntity, null, null, RECORD_LIKE, UNREAD);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationEntity> page = notificationRepository.findByUsersToEntityIdAndType(userEntity.getId(), RECORD_LIKE, pageRequest);

        //then
        Assertions.assertThat(page.getContent())
                .hasSize(2)
                .extracting("type")
                .containsOnly(RECORD_LIKE);
    }

    @Test
    @DisplayName("알림 타입으로 조회했을때 해당되는 알림 타입이 없으면 빈 배열을 반환한다")
    void findByUsersToEntityAndTypeOrderByCreatedDateTimeDescWhenTypeIsEmptyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        NotificationEntity notificationEntity1 = createNotification(userEntity, null, null, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity2 = createNotification(userEntity, null, null, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity3 = createNotification(userEntity, null, null, RECORD_LIKE, UNREAD);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<NotificationEntity> page = notificationRepository.findByUsersToEntityIdAndType(userEntity.getId(), COMMENT, pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("기록 아이디로 알림 리스트를 soft delete 한다")
    void deleteAllByRecordEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        NotificationEntity notificationEntity1 = createNotification(userEntity, feedEntity, recordEntity, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity2 = createNotification(userEntity, feedEntity, recordEntity, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity3 = createNotification(userEntity, feedEntity, recordEntity, RECORD_LIKE, UNREAD);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //when
        notificationRepository.deleteAllByRecordEntityId(recordEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("피드 아이디로 알림 리스트를 soft delete 한다")
    void deleteAllByFeedEntityIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        NotificationEntity notificationEntity1 = createNotification(userEntity, feedEntity, recordEntity, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity2 = createNotification(userEntity, feedEntity, recordEntity, RECORD_LIKE, UNREAD);
        NotificationEntity notificationEntity3 = createNotification(userEntity, feedEntity, recordEntity, RECORD_LIKE, UNREAD);
        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //when
        notificationRepository.deleteAllByFeedEntityId(feedEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("알림 아이디와 사용자 아이디로 알림을 조회한다")
    void findByIdAndUsersToEntityIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        NotificationEntity notificationEntity = notificationRepository.save(createNotification(userEntity, null, null, RECORD_LIKE, UNREAD));

        //when
        Optional<NotificationEntity> optional = notificationRepository.findByIdAndUsersToEntityId(notificationEntity.getId(), userEntity.getId());

        //then
        Assertions.assertThat(optional)
                .isPresent()
                .hasValueSatisfying(
                        entity -> {
                            Assertions.assertThat(entity).isEqualTo(notificationEntity);
                        }
                );
    }

    private NotificationEntity createNotification(UserEntity userEntity, FeedEntity feedEntity, RecordEntity recordEntity, NotificationType type, NotificationStatus status) {
        NotificationArgs args = NotificationArgs.builder()
                .feedEntity(feedEntity)
                .recordEntity(recordEntity)
                .build();

        return NotificationEntity.builder()
                .usersToEntity(userEntity)
                .args(args)
                .type(type)
                .status(status)
                .build();
    }
}