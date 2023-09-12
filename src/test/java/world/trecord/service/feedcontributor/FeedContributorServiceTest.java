package world.trecord.service.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEventListener;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.feedcontributor.request.FeedExpelRequest;
import world.trecord.service.feedcontributor.request.FeedInviteRequest;
import world.trecord.service.feedcontributor.response.UserFeedContributorListResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static world.trecord.domain.feedcontributor.FeedContributorStatus.LEFT;

@Transactional
@IntegrationTestSupport
class FeedContributorServiceTest extends AbstractContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    FeedContributorRepository feedContributorRepository;

    @Autowired
    FeedContributorService feedContributorService;

    @MockBean
    NotificationEventListener mockEventListener;

    @Test
    @DisplayName("피드 주인이 다른 사용자를 피드에 초대하면 초대된 사용자는 피드 매니저가 된다")
    void inviteUserManagerTest() throws Exception {
        //given
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
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
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        feedContributorRepository.save(createFeedContributor(invitedUser, feedEntity));

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
        UserEntity owner = userRepository.save(createUser("owner@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner));
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
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity other = createUser("other@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, other, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = userRepository.save(createUser("owner@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity alreadyInvitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, alreadyInvitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        feedContributorRepository.save(createFeedContributor(alreadyInvitedUser, feedEntity));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        feedContributorRepository.save(createFeedContributor(invitedUser, feedEntity));

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
        UserEntity owner = userRepository.save(createUser("owner@email.com"));
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
        UserEntity owner = createUser("owner@email.com");
        UserEntity other = createUser("other@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, other, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity other = createUser("other@email.com");
        userRepository.saveAll(List.of(owner, other));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = userRepository.save(createUser("owner@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

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
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity1 = createFeed(owner);
        FeedEntity feedEntity2 = createFeed(owner);
        FeedEntity feedEntity3 = createFeed(owner);
        FeedEntity feedEntity4 = createFeed(owner);
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        FeedContributorEntity feedContributor1 = createFeedContributor(invitedUser, feedEntity1);
        FeedContributorEntity feedContributor2 = createFeedContributor(invitedUser, feedEntity2);
        FeedContributorEntity feedContributor3 = createFeedContributor(invitedUser, feedEntity3);
        FeedContributorEntity feedContributor4 = createFeedContributor(invitedUser, feedEntity4);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));

        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<UserFeedContributorListResponse> page = feedContributorService.getUserParticipatingFeeds(invitedUser.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).hasSize(4);
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

    private FeedContributorEntity createFeedContributor(UserEntity userEntity, FeedEntity feedEntity) {
        return FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}