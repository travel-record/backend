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
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeListResponse;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.domain.notification.NotificationStatus.UNREAD;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;

@IntegrationTestSupport
class UserRecordLikeServiceTest extends ContainerBaseTest {

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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        userRecordLikeRepository.save(createRecordLike(userEntity, recordEntity));

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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

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
    @DisplayName("기록 작성자가 아닌 사용자가 기록에 좋아요를 하면 기록 작성자를 향한 좋아요 알림을 생성한다")
    void createNotificationTestWhenViewerLikeOnRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test1@email.com"));
        UserEntity viewer = userRepository.save(createUser("test2@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

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
        UserEntity writer = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        //when
        userRecordLikeService.toggleLike(writer.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 조회하여 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);
        RecordEntity recordEntity3 = createRecord(feedEntity);
        RecordEntity recordEntity4 = createRecord(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(userEntity, recordEntity4);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when
        UserRecordLikeListResponse response = userRecordLikeService.getUserRecordLikeList(userEntity.getId());

        //then
        Assertions.assertThat(response.getRecords())
                .hasSize(2)
                .extracting("recordId", "title", "authorNickname", "imageUrl")
                .containsExactly(
                        tuple(recordEntity4.getId(), recordEntity4.getTitle(), userEntity.getNickname(), recordEntity4.getImageUrl()),
                        tuple(recordEntity1.getId(), recordEntity1.getTitle(), userEntity.getNickname(), recordEntity1.getImageUrl())
                );
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록이 없으면 UserRecordLikeListResponse의 records 필드를 빈 배열로 반환한다")
    void getUserRecordLikeListWithEmptyListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        UserRecordLikeListResponse response = userRecordLikeService.getUserRecordLikeList(userEntity.getId());

        //then
        Assertions.assertThat(response.getRecords()).isEmpty();
    }


    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트에서 soft delete한 좋아요 리스트를 제외한 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByWhenUserLikedCancelTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(other, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(other, recordEntity2);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        userRecordLikeRepository.softDeleteById(userRecordLikeEntity2.getId());

        //when
        UserRecordLikeListResponse response = userRecordLikeService.getUserRecordLikeList(other.getId());

        //then
        Assertions.assertThat(response.getRecords())
                .hasSize(1)
                .extracting("recordId")
                .containsOnly(recordEntity1.getId());
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

    private UserRecordLikeEntity createRecordLike(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }

}