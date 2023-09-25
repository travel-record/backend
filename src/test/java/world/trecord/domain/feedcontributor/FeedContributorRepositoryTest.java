package world.trecord.domain.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;

import static world.trecord.domain.feedcontributor.FeedContributorStatus.EXPELLED;

@Transactional
class FeedContributorRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("피드 아이디로 피드 컨트리뷰터를 soft delete한다")
    void deleteAllByFeedEntityIdTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("email@email.com");
        UserEntity userEntity1 = UserEntityFixture.of("email1@email.com");
        UserEntity userEntity2 = UserEntityFixture.of("email2@email.com");
        UserEntity userEntity3 = UserEntityFixture.of("email3@email.com");
        userRepository.saveAll(List.of(owner, userEntity1, userEntity2, userEntity3));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedContributorEntity contributor1 = FeedContributorFixture.of(userEntity1, feedEntity);
        FeedContributorEntity contributor2 = FeedContributorFixture.of(userEntity2, feedEntity);
        FeedContributorEntity contributor3 = FeedContributorFixture.of(userEntity3, feedEntity);
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
        UserEntity owner = UserEntityFixture.of("email@email.com");
        UserEntity userEntity = UserEntityFixture.of("email1@email.com");
        userRepository.saveAll(List.of(owner, userEntity));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        feedContributorRepository.save(FeedContributorFixture.of(userEntity, feedEntity));

        //when
        feedContributorRepository.deleteByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("삭제한 유저 아이디와 피드 아이디로 피드 컨트리뷰터 다시 저장한다")
    void deleteByUserEntityIdAndFeedEntityIdWhoDeletedTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("email@email.com");
        UserEntity userEntity = UserEntityFixture.of("email1@email.com");
        userRepository.saveAll(List.of(owner, userEntity));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(userEntity, feedEntity));
        feedContributorRepository.deleteByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId());

        //when
        feedContributorRepository.save(FeedContributorFixture.of(userEntity, feedEntity));

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("피드 컨트리뷰터 상태를 변경하고 soft delete한다")
    void updateStatusAndDeleteByUserEntityIdAndFeedEntityIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        feedContributorRepository.save(FeedContributorFixture.of(userEntity, feedEntity));

        //when
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(userEntity.getId(), feedEntity.getId(), EXPELLED);

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("피드 컨트리뷰터 아이디로 피드 정보와 피드 주인의 정보를 페이지네이션으로 조회한다")
    void findWithFeedEntityByUserEntityIdTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("email@email.com");
        UserEntity contributor = UserEntityFixture.of("email1@email.com");
        userRepository.saveAll(List.of(owner, contributor));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));

        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<FeedContributorEntity> result = feedContributorRepository.findWithFeedEntityByUserEntityId(contributor.getId(), pageRequest);

        //then
        Assertions.assertThat(result.getContent())
                .hasSize(1)
                .extracting("feedEntity")
                .containsExactly(feedEntity);
    }

}