package world.trecord.service.userrecordlike;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.CommitIntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;

@Slf4j
@CommitIntegrationTestSupport
public class UserRecordLikeServiceAsyncTest {

    @Autowired
    UserRecordLikeService userRecordLikeService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        userRecordLikeRepository.deleteAll();
        recordRepository.deleteAll();
        feedRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("기록 작성자가 아닌 사용자가 기록에 좋아요를 하면 비동기로 기록 작성자를 향한 좋아요 알림을 생성한다")
    void createNotificationTestWhenViewerLikeOnRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser());
        UserEntity viewer = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        //when
        log.info("toggleLike started in thread: {}", Thread.currentThread().getName());
        final CountDownLatch latch = new CountDownLatch(1);

        userRecordLikeService.toggleLike(viewer.getId(), recordEntity.getId());

        latch.await(1, TimeUnit.SECONDS);
        log.info("toggleLike ended in thread: {}", Thread.currentThread().getName());

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .hasSize(1)
                .extracting("type", "status")
                .containsExactly(
                        tuple(RECORD_LIKE, UNREAD)
                );
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email(UUID.randomUUID().toString() + System.currentTimeMillis() + Thread.currentThread().getId())
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2022, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 9, 30, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }
}
