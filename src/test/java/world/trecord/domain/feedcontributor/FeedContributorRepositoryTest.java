package world.trecord.domain.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.users.response.UserResponse;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;
import java.util.Optional;

import static world.trecord.domain.feedcontributor.FeedContributorStatus.EXPELLED;
import static world.trecord.domain.feedcontributor.FeedContributorStatus.PARTICIPATING;

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

    @CsvSource({"LEFT", "EXPELLED"})
    @ParameterizedTest
    @DisplayName("현재 피드에 참여 중인 피드 컨트리뷰터이면 참여중 상태를 반환한다")
    void findTopByUserEntityIdAndFeedEntityIdOrderByCreatedDateTimeDesc_whenUserParticipating_returnStatusParticipating(FeedContributorStatus status) throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity contributor = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, contributor));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(contributor.getId(), feedEntity.getId(), status);
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        entityManager.flush();
        entityManager.clear();

        //when
        Optional<FeedContributorEntity> feedOpt = feedContributorRepository.findTopByUserIdAndFeedIdOrderByModifiedAtDesc(contributor.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(feedOpt)
                .isPresent()
                .hasValueSatisfying(
                        it -> {
                            Assertions.assertThat(it.getStatus()).isEqualByComparingTo(PARTICIPATING);
                        }
                );
    }

    @CsvSource({"LEFT", "EXPELLED"})
    @ParameterizedTest
    @DisplayName("가장 최근에 피드에서 나갔거나, 쫓겨난 사용자는 피드에 참여중 상태가 아니다")
    void findTopByUserEntityIdAndFeedEntityIdOrderByCreatedDateTimeDesc_whenUserExpelledOrLeaved_returnStatusParticipating(FeedContributorStatus status) throws Exception {
        UserEntity owner = UserEntityFixture.of();
        UserEntity contributor = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, contributor));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(contributor.getId(), feedEntity.getId(), status);
        entityManager.flush();
        entityManager.clear();

        //when
        Optional<FeedContributorEntity> feedOpt = feedContributorRepository.findTopByUserIdAndFeedIdOrderByModifiedAtDesc(contributor.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(feedOpt)
                .isPresent()
                .hasValueSatisfying(
                        it -> {
                            Assertions.assertThat(it.getStatus()).isNotEqualByComparingTo(PARTICIPATING);
                        }
                );
    }

    @Test
    @DisplayName("피드 컨트리뷰터로 참여하지 않았던 사용자는 empty를 반환한다")
    void findTopByUserEntityIdAndFeedEntityIdOrderByCreatedDateTimeDesc_whenUserNotParticipating_returnStatusParticipating() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity other = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, other));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        //when
        Optional<FeedContributorEntity> feedOpt = feedContributorRepository.findTopByUserIdAndFeedIdOrderByModifiedAtDesc(other.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(feedOpt).isEmpty();
    }

    @Test
    @DisplayName("사용자가 최근에 피드에 초대한 유니크한 사용자들을 최대 3명 조회한다")
    void findRecentContributorsByUserId_returnUniqueUserInfo() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity invitee1 = UserEntityFixture.of();
        UserEntity invitee2 = UserEntityFixture.of();
        UserEntity invitee3 = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitee1, invitee2, invitee3));

        FeedEntity feed1 = FeedEntityFixture.of(owner);
        FeedEntity feed2 = FeedEntityFixture.of(owner);
        FeedEntity feed3 = FeedEntityFixture.of(owner);
        feedRepository.saveAll(List.of(feed1, feed2, feed3));

        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(invitee1, feed1); // feed1 -> invitee1 초대
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(invitee1, feed2); // feed2 -> invitee1 + invitee2 초대
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(invitee2, feed2);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(invitee1, feed3); // feed3 -> invitee1 + invitee3 초대
        FeedContributorEntity feedContributor5 = FeedContributorFixture.of(invitee3, feed3);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4, feedContributor5));

        //when
        List<Object[]> objects = feedContributorRepository.findRecentMaxThreeContributorsByUserId(owner.getId());

        //then
        List<UserResponse> responses = objects.stream()
                .map(UserResponse::of)
                .toList();

        Assertions.assertThat(responses)
                .hasSize(3)
                .extracting("userId")
                .containsOnly(invitee1.getId(), invitee2.getId(), invitee3.getId());
    }
}