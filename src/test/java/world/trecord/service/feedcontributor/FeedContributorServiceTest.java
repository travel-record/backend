package world.trecord.service.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.feedcontributor.request.FeedExpelRequest;
import world.trecord.dto.feedcontributor.request.FeedInviteRequest;
import world.trecord.dto.feedcontributor.response.FeedInvitationHistoryResponse;
import world.trecord.dto.feedcontributor.response.UserFeedContributorListResponse;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static world.trecord.domain.feedcontributor.FeedContributorStatus.LEFT;

@Transactional
class FeedContributorServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("피드 주인이 다른 사용자를 피드에 초대하면 초대된 사용자는 피드 매니저가 된다")
    void inviteUserManagerTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when
        feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request);

        //then
        Assertions.assertThat(feedContributorRepository.findAll())
                .hasSize(1)
                .extracting("userEntity")
                .containsOnly(invitedUser);
    }

    @Test
    @DisplayName("존재하지 않는 피드에 사용자를 초대할 수 없다")
    void inviteUserInNotExistingFeedThrowExceptionTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        long notExistingFeedId = -1L;

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.inviteUserToFeed(owner.getId(), notExistingFeedId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("피드에서 나간 사용자를 다시 초대할 수 있다")
    void inviteUserWhoLeaveFeedBeforeTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        feedEntity.removeFeedContributor(invitedUser.getId());
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(invitedUser.getId(), feedEntity.getId(), LEFT);

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when
        feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request);

        //then
        Assertions.assertThat(feedContributorRepository.findAll())
                .hasSize(1)
                .extracting("userEntity")
                .containsOnly(invitedUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 초대할 수 없다")
    void inviteNotExistingUserThrowExceptionTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("owner@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        long notExistingUserId = -1L;

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(notExistingUserId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("피드에 초대된 사용자는 자신의 글에 대한 모든 권한을 가진다")
    void managerPermissionTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when
        feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request);

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("피드 주인이 아닌 사람이 다른 사용자를 초대하면 예외가 발생한다")
    void inviteUserWhenNotOwnerTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity other = UserEntityFixture.of("other@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, other, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.inviteUserToFeed(other.getId(), feedEntity.getId(), null))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("피드 주인이 자기 자신을 초대하면 예외가 발생한다")
    void inviteUserWhenSelfInviteTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("owner@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(owner.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.SELF_INVITATION_NOT_ALLOWED);
    }

    @Test
    @DisplayName("피드 주인이 다른 사용자를 초대하면 초대된 사용자에게 알림이 전송된다")
    void inviteUserNotificationTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when
        feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request);

        //then
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Mockito.verify(mockEventListener, Mockito.times(1)).handleNotificationEventListener(Mockito.any()));
    }

    @Test
    @DisplayName("피드에 이미 초대된 사용자에게 피드 초대를 하면 예외가 발생한다")
    void inviteUserWhoAlreadyInvitedTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity alreadyInvitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, alreadyInvitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        feedContributorRepository.save(FeedContributorFixture.of(alreadyInvitedUser, feedEntity));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(alreadyInvitedUser.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_ALREADY_INVITED);
    }

    @Test
    @DisplayName("피드 주인이 피드에 초대된 사용자를 내보내면 피드 컨트리뷰터에서 제거된다")
    void expelUserTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when
        feedContributorService.expelUserFromFeed(owner.getId(), request.getUserToId(), feedEntity.getId());

        //then
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 피드에서 사용자를 내보내려고 하면 예외가 발생한다")
    void expelUserWhenNotExistingFeedTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("owner@email.com"));
        long notExistingFeedId = 0L;

        FeedExpelRequest request = FeedExpelRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.expelUserFromFeed(owner.getId(), request.getUserToId(), notExistingFeedId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 주인이 아닌 사람이 사용자를 내보려고 하면 예외가 발생한다")
    void expelUserByNotFeedOwnerTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity other = UserEntityFixture.of("other@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, other, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.expelUserFromFeed(other.getId(), invitedUser.getId(), feedEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("피드 컨트리뷰터가 아닌 사용자를 내보려고 하면 예외가 발생한다")
    void expelNotFeedContributorUserTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity other = UserEntityFixture.of("other@email.com");
        userRepository.saveAll(List.of(owner, other));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(other.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.expelUserFromFeed(owner.getId(), request.getUserToId(), feedEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_INVITED);
    }

    @Test
    @DisplayName("피드 주인이 자기 자신을 내보낼 수 없다")
    void expelUserWhenFeedOwnerSelfExpelTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("owner@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(owner.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedContributorService.expelUserFromFeed(owner.getId(), request.getUserToId(), feedEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.SELF_EXPELLING_NOT_ALLOWED);
    }

    @Test
    @DisplayName("자신이 피드 컨트리뷰터로 참여하는 피드 정보와 피드 주인 정보를 페이지네이션으로 조회한다")
    void getUserFeedContributorsTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity1 = FeedEntityFixture.of(owner);
        FeedEntity feedEntity2 = FeedEntityFixture.of(owner);
        FeedEntity feedEntity3 = FeedEntityFixture.of(owner);
        FeedEntity feedEntity4 = FeedEntityFixture.of(owner);
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(invitedUser, feedEntity1);
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(invitedUser, feedEntity2);
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(invitedUser, feedEntity3);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(invitedUser, feedEntity4);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));

        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<UserFeedContributorListResponse> page = feedContributorService.getUserParticipatingFeeds(invitedUser.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("피드 컨트리뷰터를 내보낼때 피드 컨트리뷰터가 작성한 기록이 삭제된다")
    void expelUserFromFeedAndDeleteRecordWhenFeedContributorExpelledTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        recordRepository.save(RecordEntityFixture.of(invitedUser, feedEntity, 1));

        //when
        feedContributorService.expelUserFromFeed(owner.getId(), invitedUser.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("피드 컨트리뷰터가 피드를 나갈 때 자신이 작성한 기록이 삭제된다")
    void leaveFeedAndDeleteRecordWhenFeedContributorExpelledTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity invitedUser = UserEntityFixture.of("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        FeedContributorEntity feedContributor = feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        recordRepository.save(RecordEntityFixture.of(invitedUser, feedEntity, 1));

        //when
        feedContributorService.leaveFeed(invitedUser.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 가장 최근에 피드에 초대한 유니크한 사용자들을 최대 3명 조회한다")
    void getRecentUniqueMaxThreeInvitees_returnUserResponseList() throws Exception {
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
        FeedInvitationHistoryResponse response = feedContributorService.getRecentUniqueMaxThreeInvitees(owner.getId());

        //then
        Assertions.assertThat(response.getContent())
                .hasSize(3)
                .extracting("userId")
                .containsOnly(invitee1.getId(), invitee2.getId(), invitee3.getId());
    }

    @Test
    @DisplayName("사용자가 초대한 기록이 없으면 빈 배열을 반환한다")
    void getRecentUniqueMaxThreeInvitees_returnEmptyList() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());

        //when
        FeedInvitationHistoryResponse response = feedContributorService.getRecentUniqueMaxThreeInvitees(owner.getId());

        //then
        Assertions.assertThat(response.getContent()).isEmpty();
    }
}