package world.trecord.domain.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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

@Transactional
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));
        UserRecordLikeEntity userRecordLikeEntity = createRecordLike(userEntity, recordEntity);
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        //when
        Optional<UserRecordLikeEntity> likeEntity = userRecordLikeRepository.findByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(likeEntity).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 등록 시간 내림차 순으로 projection으로 반환한다")
    void findLikedRecordsByUserEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = createRecord(feedEntity, 1);
        RecordEntity recordEntity2 = createRecord(feedEntity, 2);
        RecordEntity recordEntity3 = createRecord(feedEntity, 3);
        RecordEntity recordEntity4 = createRecord(feedEntity, 4);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(userEntity, recordEntity4);
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        List<UserRecordProjection> projectionList = userRecordLikeRepository.findLikeRecordsByUserEntityId(userEntity.getId());

        //then
        Assertions.assertThat(projectionList).isEmpty();
    }

    @Test
    @DisplayName("사용자가 기록에 좋아요 하였으면 조회 시 true를 반환한다")
    void existsByUserEntityAndRecordEntityWhenUserLikedRecordTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));
        UserRecordLikeEntity userRecordLikeEntity = createRecordLike(userEntity, recordEntity);
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자가 null이면 기록에 좋아요 하였는지 조회 시 false를 반환한다")
    void existsByUserEntityAndRecordEntityWhenNullUserEntityest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(null, recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기록으로 좋아요 리스트를 soft delete한다")
    void deleteAllByRecordEntityTest() throws Exception {
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        UserEntity other1 = userRepository.save(createUser("test1@email.com"));
        UserEntity other2 = userRepository.save(createUser("test2@email.com"));
        UserEntity other3 = userRepository.save(createUser("test3@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(other1, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(other2, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity3 = createRecordLike(other3, recordEntity);

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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        UserEntity other1 = userRepository.save(createUser("test1@email.com"));
        UserEntity other2 = userRepository.save(createUser("test2@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(other1, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(other2, recordEntity);
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));
        UserRecordLikeEntity userRecordLikeEntity = userRecordLikeRepository.save(createRecordLike(other, recordEntity));
        userRecordLikeRepository.delete(userRecordLikeEntity);

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
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
                .startAt(LocalDateTime.of(2022, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2022, 3, 5, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("title")
                .place("place")
                .date(LocalDateTime.of(2022, 3, 1, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
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