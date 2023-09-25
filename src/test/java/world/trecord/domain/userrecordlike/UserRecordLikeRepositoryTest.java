package world.trecord.domain.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.fixture.UserRecordLikeFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;
import java.util.Optional;

@Transactional
class UserRecordLikeRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자가 좋아요한 기록이 존재하면 UserRecordLikeEntity를 반환한다")
    void existsUserRecordLikeEntityByUserEntityAndRecordEntityTestWhenUserLikeRecordExists() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));
        UserRecordLikeEntity userRecordLikeEntity = UserRecordLikeFixture.of(userEntity, recordEntity);
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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));

        //when
        Optional<UserRecordLikeEntity> likeEntity = userRecordLikeRepository.findByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(likeEntity).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 페이지네이션으로 반환한다")
    void findLikedRecordsByUserEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        RecordEntity recordEntity1 = RecordEntityFixture.of(userEntity, feedEntity, 1);
        RecordEntity recordEntity2 = RecordEntityFixture.of(userEntity, feedEntity, 2);
        RecordEntity recordEntity3 = RecordEntityFixture.of(userEntity, feedEntity, 3);
        RecordEntity recordEntity4 = RecordEntityFixture.of(userEntity, feedEntity, 4);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = UserRecordLikeFixture.of(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = UserRecordLikeFixture.of(userEntity, recordEntity4);
        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        int pageNumber = 0;
        int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<UserRecordProjection> page = userRecordLikeRepository.findLikeRecordsByUserId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent())
                .hasSize(2)
                .extracting("id")
                .containsOnly(recordEntity1.getId(), recordEntity4.getId());
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록이 없으면 빈 배열을 반환한다")
    void findLikedRecordsByUserEntityWithNotExistingLikeTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        int pageNumber = 0;
        int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<UserRecordProjection> page = userRecordLikeRepository.findLikeRecordsByUserId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 기록에 좋아요 하였으면 조회 시 true를 반환한다")
    void existsByUserEntityAndRecordEntityWhenUserLikedRecordTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));
        UserRecordLikeEntity userRecordLikeEntity = UserRecordLikeFixture.of(userEntity, recordEntity);
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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자가 null이면 기록에 좋아요 하였는지 조회 시 false를 반환한다")
    void existsByUserEntityAndRecordEntityWhenNullUserEntityest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(null, recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기록으로 좋아요 리스트를 soft delete한다")
    void deleteAllByRecordEntityTest() throws Exception {
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other1 = userRepository.save(UserEntityFixture.of("test1@email.com"));
        UserEntity other2 = userRepository.save(UserEntityFixture.of("test2@email.com"));
        UserEntity other3 = userRepository.save(UserEntityFixture.of("test3@email.com"));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));

        UserRecordLikeEntity userRecordLikeEntity1 = UserRecordLikeFixture.of(other1, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity2 = UserRecordLikeFixture.of(other2, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity3 = UserRecordLikeFixture.of(other3, recordEntity);

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
        UserEntity userEntity = UserEntityFixture.of();
        UserEntity other1 = UserEntityFixture.of();
        UserEntity other2 = UserEntityFixture.of();
        userRepository.saveAll(List.of(userEntity, other1, other2));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));

        UserRecordLikeEntity userRecordLikeEntity1 = UserRecordLikeFixture.of(other1, recordEntity);
        UserRecordLikeEntity userRecordLikeEntity2 = UserRecordLikeFixture.of(other2, recordEntity);
        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when
        userRecordLikeRepository.delete(userRecordLikeEntity1);

        //then
        Assertions.assertThat(userRecordLikeRepository.findAll())
                .hasSize(1)
                .containsOnly(userRecordLikeEntity2);
    }

    @Test
    @DisplayName("사용자가 좋아요 soft delete 한  기록에는 조회 시 true를 반환한다")
    void existsByUserEntityAndRecordEntityWhenRecordSoftDeletedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntityFixture.of();
        UserEntity other = UserEntityFixture.of();
        userRepository.saveAll(List.of(userEntity, other));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(userEntity, feedEntity, 1));
        UserRecordLikeEntity userRecordLikeEntity = userRecordLikeRepository.save(UserRecordLikeFixture.of(other, recordEntity));
        userRecordLikeRepository.delete(userRecordLikeEntity);

        //when
        boolean result = userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }
}