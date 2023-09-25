package world.trecord.domain.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
class FeedRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("유저 엔티티로 피드 리스트를 조회할 때 사용자가 등록한 피드 리스트를 페이지네이션으로 조회한다")
    void findByUserEntityOrderByStartAtDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity1 = createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(userEntity, LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeed(userEntity, LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));

        final int pageNumber = 0;
        final int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<FeedEntity> page = feedRepository.findByUserEntityId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("피드를 조회할 때 피드 주인과 피드 컨트리뷰터 정보를 함께 조회한다")
    void findWithFeedContributorsById() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity contributor1 = UserEntityFixture.of();
        UserEntity contributor2 = UserEntityFixture.of();
        UserEntity contributor3 = UserEntityFixture.of();
        UserEntity contributor4 = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, contributor1, contributor2, contributor3, contributor4));

        userRepository.saveAll(List.of(owner, contributor1, contributor2, contributor3, contributor4));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(contributor1, feedEntity);
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(contributor2, feedEntity);
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(contributor3, feedEntity);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(contributor4, feedEntity);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));

        //when
        Optional<FeedEntity> feedEntityOpt = feedRepository.findWithOwnerAndParticipatingContributorsById(feedEntity.getId());

        //then
        Assertions.assertThat(feedEntityOpt)
                .isPresent()
                .hasValueSatisfying(
                        it -> {
                            UserEntity userEntity = it.getUserEntity();
                            List<FeedContributorEntity> contributors = it.getContributors();
                            Assertions.assertThat(userEntity.getId()).isEqualTo(owner.getId());
                            Assertions.assertThat(it.getContributors()).containsAll(contributors);
                        }
                );
    }

    @Test
    @DisplayName("피드를 조회할 때 쓰기락을 걸고 피드 컨트리뷰터와 함께 조회한다")
    void findWithFeedContributorsByIdForUpdateTest() throws Exception {
        //given
        UserEntity userEntity = UserEntityFixture.of();
        UserEntity contributor1 = UserEntityFixture.of();
        UserEntity contributor2 = UserEntityFixture.of();
        UserEntity contributor3 = UserEntityFixture.of();
        UserEntity contributor4 = UserEntityFixture.of();
        userRepository.saveAll(List.of(userEntity, contributor1, contributor2, contributor3, contributor4));

        LocalDateTime feedTime = LocalDateTime.of(2021, 10, 2, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, feedTime, feedTime));

        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(contributor1, feedEntity);
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(contributor2, feedEntity);
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(contributor3, feedEntity);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(contributor4, feedEntity);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));

        //when
        Optional<FeedEntity> optionalFeed = feedRepository.findWithFeedContributorsByIdForUpdate(feedEntity.getId());

        //then
        Assertions.assertThat(optionalFeed)
                .isPresent()
                .hasValueSatisfying(
                        entity -> {
                            Assertions.assertThat(entity.getFeedContributors())
                                    .containsAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));
                        }
                );
    }

    @Test
    @DisplayName("유저 엔티티로 피드 리스트를 조회할 때 사용자가 등록한 피드가 없으면 빈 리스트가 반환된다")
    void findByUserEntityOrderByStartAtDescWithEmptyFeedListTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        final int pageNumber = 0;
        final int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<FeedEntity> page = feedRepository.findByUserEntityId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("피드를 soft delete 한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        //when
        feedRepository.delete(feedEntity);

        //then
        Assertions.assertThat(feedRepository.findAll()).isEmpty();
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }
}