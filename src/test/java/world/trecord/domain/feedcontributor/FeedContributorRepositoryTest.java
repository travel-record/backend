package world.trecord.domain.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@IntegrationTestSupport
class FeedContributorRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    FeedContributorRepository feedContributorRepository;

    @Test
    @DisplayName("사용자가 피드의 컨트리뷰터로 존재하면 true를 반환한다")
    void existsByUserEntityIdAndFeedEntityIdReturnsTrueTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("email@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        feedContributorRepository.save(createContributor(userEntity, feedEntity));

        //when
        boolean result = feedContributorRepository.existsByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 피드의 컨트리뷰터로 존재하지 않으면 false를 반환한다")
    void existsByUserEntityIdAndFeedEntityIdReturnsFalseTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("email@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        //when
        boolean result = feedContributorRepository.existsByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("피드 아이디로 피드 컨트리뷰터를 soft delete한다")
    void deleteAllByFeedEntityIdTest() throws Exception {
        //given
        UserEntity owner = createUser("email@email.com");
        UserEntity userEntity1 = createUser("email1@email.com");
        UserEntity userEntity2 = createUser("email2@email.com");
        UserEntity userEntity3 = createUser("email3@email.com");
        userRepository.saveAll(List.of(owner, userEntity1, userEntity2, userEntity3));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        FeedContributorEntity contributor1 = createContributor(userEntity1, feedEntity);
        FeedContributorEntity contributor2 = createContributor(userEntity2, feedEntity);
        FeedContributorEntity contributor3 = createContributor(userEntity3, feedEntity);
        feedContributorRepository.saveAll(List.of(contributor1, contributor2, contributor3));

        //when
        feedContributorRepository.deleteAllByFeedEntityId(feedEntity.getId());

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("유저 아이디와 피드 아이디로 피드 컨트리뷰터를 soft delete 한다")
    void deleteByUserEntityIdAndFeedEntityIdTest() throws Exception {
        //given
        UserEntity owner = createUser("email@email.com");
        UserEntity userEntity = createUser("email1@email.com");
        userRepository.saveAll(List.of(owner, userEntity));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        feedContributorRepository.save(createContributor(userEntity, feedEntity));

        //when
        feedContributorRepository.deleteByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("삭제한 유저 아이디와 피드 아이디로 피드 컨트리뷰터 다시 저장한다")
    void deleteByUserEntityIdAndFeedEntityIdWhoDeletedTest() throws Exception {
        //given
        UserEntity owner = createUser("email@email.com");
        UserEntity userEntity = createUser("email1@email.com");
        userRepository.saveAll(List.of(owner, userEntity));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner));
        feedContributorRepository.save(createContributor(userEntity, feedEntity));
        feedContributorRepository.deleteByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //when
        feedContributorRepository.save(createContributor(userEntity, feedEntity));

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
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
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now())
                .build();
    }

    private FeedContributorEntity createContributor(UserEntity userEntity, FeedEntity feedEntity) {
        return FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}