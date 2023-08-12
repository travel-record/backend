package world.trecord.domain.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;

import java.util.List;

import static world.trecord.domain.notification.NotificationStatus.READ;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.COMMENT;

@IntegrationTestSupport
class NotificationRepositoryTest {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("사용자에게 읽지 않은 알림이 있으면 새로운 알림이 있음을 반환한다")
    void existsByUsersToEntityIdAndUnreadStatusTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        NotificationEntity notificationEntity = createNotificationEntity(userEntity, UNREAD);

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
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        NotificationEntity notificationEntity = createNotificationEntity(userEntity, READ);

        notificationRepository.save(notificationEntity);

        //when
        boolean result = notificationRepository.existsByUsersToEntityIdAndStatus(userEntity.getId(), UNREAD);

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자의 알림이 생성된 시간 내림차순으로 조회한다")
    void findByUsersToEntityOrderByCreatedDateTimeDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        NotificationEntity notificationEntity1 = createNotificationEntity(userEntity, READ);
        NotificationEntity notificationEntity2 = createNotificationEntity(userEntity, READ);
        NotificationEntity notificationEntity3 = createNotificationEntity(userEntity, READ);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //when
        List<NotificationEntity> notificationList = notificationRepository.findByUsersToEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        //then
        Assertions.assertThat(notificationList)
                .hasSize(3)
                .containsExactly(notificationEntity3, notificationEntity2, notificationEntity1);
    }

    @Transactional
    @Test
    @DisplayName("사용자가 읽지 않은 알림을 모두 읽음 처리하여 처리된 개수를 반환한다")
    void updateNotificationStatusByUserIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        NotificationEntity notificationEntity1 = createNotificationEntity(userEntity, UNREAD);
        NotificationEntity notificationEntity2 = createNotificationEntity(userEntity, READ);
        NotificationEntity notificationEntity3 = createNotificationEntity(userEntity, UNREAD);

        notificationRepository.saveAll(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

        //when
        int count = notificationRepository.updateNotificationStatusByUserId(userEntity.getId(), UNREAD, READ);

        //then
        Assertions.assertThat(count).isEqualTo(2);
        Assertions.assertThat(notificationRepository.findAll())
                .filteredOn(notificationEntity -> notificationEntity.getStatus().equals(READ))
                .hasSize(3);
    }

    private NotificationEntity createNotificationEntity(UserEntity userEntity, NotificationStatus notificationStatus) {
        return NotificationEntity.builder()
                .usersToEntity(userEntity)
                .type(COMMENT)
                .status(notificationStatus)
                .build();
    }

}