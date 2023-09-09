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
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        feedContributorRepository.save(createManager(userEntity, feedEntity));

        //when
        boolean result = feedContributorRepository.existsByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 피드의 컨트리뷰터로 존재하지 않으면 false를 반환한다")
    void existsByUserEntityIdAndFeedEntityIdReturnsFalseTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        //when
        boolean result = feedContributorRepository.existsByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email("email@email.com")
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

    private FeedContributorEntity createManager(UserEntity userEntity, FeedEntity feedEntity) {
        return FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}