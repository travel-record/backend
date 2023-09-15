package world.trecord.domain.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@IntegrationTestSupport
class FeedRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    FeedContributorRepository feedContributorRepository;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @DisplayName("유저 엔티티로 피드 리스트를 조회할 때 사용자가 등록한 피드 리스트를 여행 시작 시간 내림차순으로 조회한다")
    void findByUserEntityOrderByStartAtDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity1 = createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(userEntity, LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeed(userEntity, LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));

        //when
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userEntity.getId());

        //then
        Assertions.assertThat(feedEntities)
                .extracting("id")
                .containsExactly(
                        feedEntity3.getId(), feedEntity2.getId(), feedEntity1.getId()
                );
    }

    @Test
    @DisplayName("피드를 조회할 때 피드 컨트리뷰터와 함께 조회한다")
    void findWithFeedContributorsByIdForUpdateTest() throws Exception {
        //given
        UserEntity userEntity = createUser("test@email.com");
        UserEntity contributor1 = createUser("test1@email.com");
        UserEntity contributor2 = createUser("test2@email.com");
        UserEntity contributor3 = createUser("test3@email.com");
        UserEntity contributor4 = createUser("test4@email.com");
        userRepository.saveAll(List.of(userEntity, contributor1, contributor2, contributor3, contributor4));

        LocalDateTime feedTime = LocalDateTime.of(2021, 10, 2, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, feedTime, feedTime));

        FeedContributorEntity feedContributor1 = createFeedContributor(contributor1, feedEntity);
        FeedContributorEntity feedContributor2 = createFeedContributor(contributor2, feedEntity);
        FeedContributorEntity feedContributor3 = createFeedContributor(contributor3, feedEntity);
        FeedContributorEntity feedContributor4 = createFeedContributor(contributor4, feedEntity);
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
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userEntity.getId());

        //then
        Assertions.assertThat(feedEntities).isEmpty();
    }

    @Test
    @DisplayName("피드를 soft delete 한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        //when
        feedRepository.delete(feedEntity);

        //then
        Assertions.assertThat(feedRepository.findAll()).isEmpty();
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private FeedContributorEntity createFeedContributor(UserEntity userEntity, FeedEntity feedEntity) {
        return FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}