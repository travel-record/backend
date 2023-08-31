package world.trecord.domain.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class UserRecordLikeRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Test
    @DisplayName("사용자가 좋아요한 기록이 존재하면 UserRecordLikeEntity를 반환한다")
    void existsUserRecordLikeEntityByUserEntityAndRecordEntityTestWhenUserLikeRecordExists() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        UserRecordLikeEntity userRecordLikeEntity = createUserRecordLikeEntity(userEntity, recordEntity);

        userRecordLikeRepository.save(userRecordLikeEntity);

        //when
        Optional<UserRecordLikeEntity> likeEntity = userRecordLikeRepository.findByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(likeEntity).isPresent();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록이 존재하지 않으면 빈 UserRecordLikeEntity를 반환한다")
    void existsUserRecordLikeEntityByUserEntityAndRecordEntityTestWhenUserLikeRecordNotExists() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        Optional<UserRecordLikeEntity> likeEntity = userRecordLikeRepository.findByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(likeEntity).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 등록 시간 내림차 순으로 projection으로 반환한다")
    void findLikedRecordsByUserEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity4 = createRecordEntity(feedEntity, "record4", "place4", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = createUserRecordLikeEntity(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createUserRecordLikeEntity(userEntity, recordEntity4);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when
        List<UserRecordProjection> projectionList = userRecordLikeRepository.findLikeRecordsByUserEntityId(userEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(2)
                .extracting("id", "title", "authorId", "authorNickname", "imageUrl")
                .containsExactly(
                        tuple(recordEntity4.getId(), recordEntity4.getTitle(), userEntity.getId(), userEntity.getNickname(), recordEntity4.getImageUrl()),
                        tuple(recordEntity1.getId(), recordEntity1.getTitle(), userEntity.getId(), userEntity.getNickname(), recordEntity1.getImageUrl())
                );
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록이 없으면 빈 배열을 반환한다")
    void findLikedRecordsByUserEntityWithNotExistingLikeTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        //when
        List<UserRecordProjection> projectionList = userRecordLikeRepository.findLikeRecordsByUserEntityId(userEntity.getId());

        //then
        Assertions.assertThat(projectionList).isEmpty();
    }

    @Test
    @DisplayName("사용자가 기록에 좋아요 하였으면 조회 시 true를 반환한다")
    void existsByUserEntityAndRecordEntityWhenUserLikedRecordTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        UserRecordLikeEntity userRecordLikeEntity = createUserRecordLikeEntity(userEntity, recordEntity);

        userRecordLikeRepository.save(userRecordLikeEntity);

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 기록에 좋아요 하지 않았으면 조회 시 false를 반환한다")
    void existsByUserEntityAndRecordEntityWhenUserNotLikedRecordTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자가 null이면 기록에 좋아요 하였는지 조회 시 false를 반환한다")
    void existsByUserEntityAndRecordEntityWhenNullUserEntityest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(null, recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기록으로 좋아요 리스트를 soft delete한다")
    void deleteAllByRecordEntityTest() throws Exception {
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        UserEntity other1 = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        UserEntity other2 = userRepository.save(UserEntity.builder().email("test2@email.com").build());
        UserEntity other3 = userRepository.save(UserEntity.builder().email("test3@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        UserRecordLikeEntity userRecordLikeEntity1 = createUserRecordLikeEntity(other1, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity2 = createUserRecordLikeEntity(other2, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity3 = createUserRecordLikeEntity(other3, recordEntity);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2, userRecordLikeEntity3));

        //when
        userRecordLikeRepository.deleteAllByRecordEntityId(recordEntity.getId());

        //then
        Assertions.assertThat(userRecordLikeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("좋아요를 soft delete한다")
    void softDeleteTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        UserEntity other1 = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        UserEntity other2 = userRepository.save(UserEntity.builder().email("test2@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        UserRecordLikeEntity userRecordLikeEntity1 = createUserRecordLikeEntity(other1, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity2 = createUserRecordLikeEntity(other2, recordEntity);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when
        userRecordLikeRepository.softDeleteById(userRecordLikeEntity1.getId());

        //then
        Assertions.assertThat(userRecordLikeRepository.findAll())
                .hasSize(1)
                .containsOnly(userRecordLikeEntity2);
    }

    @Test
    @DisplayName("사용자가 좋아요 soft delete 한  기록에는 조회 시 true를 반환한다")
    void existsByUserEntityAndRecordEntityWhenRecordSoftDeletedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        UserEntity other = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        UserRecordLikeEntity userRecordLikeEntity = userRecordLikeRepository.save(createUserRecordLikeEntity(other, recordEntity));

        userRecordLikeRepository.delete(userRecordLikeEntity);

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
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

    private UserRecordLikeEntity createUserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }
}