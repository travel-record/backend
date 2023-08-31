package world.trecord.web.service.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.IntegrationContainerBaseTest;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;

class UserRecordLikeServiceTest extends IntegrationContainerBaseTest {

    @Autowired
    UserRecordLikeService userRecordLikeService;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    NotificationRepository notificationRepository;


    @Test
    @DisplayName("사용자가 좋아요한 기록에 좋아요를 하면 liked=false 응답을 한다")
    void toggleLikeTestWhenUserAlreadyLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        userRecordLikeRepository.save(createUserRecordLikeEntity(userEntity, recordEntity));

        //when
        UserRecordLikeResponse response = userRecordLikeService.toggleLike(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(response.isLiked()).isFalse();
        Assertions.assertThat(userRecordLikeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요하지 않은 기록에 좋아요를 하면 liked=true 응답을 한다")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        UserRecordLikeResponse response = userRecordLikeService.toggleLike(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(response.isLiked()).isTrue();
        Assertions.assertThat(userRecordLikeRepository.findAll())
                .hasSize(1)
                .extracting("userEntity", "recordEntity")
                .containsExactly(
                        tuple(userEntity, recordEntity)
                );
    }

    @Test
    @DisplayName("기록 작성자가 아닌 사용자가 좋아요하지 않은 기록에 좋아요를 하면 기록 작성자를 향한 좋아요 알림을 생성한다")
    void createNotificationTestWhenViewerLikeOnRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        UserEntity viewer = userRepository.save(UserEntity.builder().email("test2@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        userRecordLikeService.toggleLike(viewer.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll())
                .hasSize(1)
                .extracting("type", "status")
                .containsExactly(
                        tuple(RECORD_LIKE, UNREAD)
                );
    }

    @Test
    @DisplayName("기록 작성자가 본인이 작성하였고 좋아요하지 않은 기록에 좋아요를 하면 기록 작성자를 향한 좋아요 알림을 생성하지 않는다")
    void createNotificationTestWhenWriterLikeOnRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        userRecordLikeService.toggleLike(writer.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }


    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private UserRecordLikeEntity createUserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
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

}