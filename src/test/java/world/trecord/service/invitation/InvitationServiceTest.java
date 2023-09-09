package world.trecord.service.invitation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.invitation.InvitationRepository;
import world.trecord.domain.invitation.InvitationStatus;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEventListener;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.invitation.request.FeedInviteRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.groups.Tuple.tuple;

@Transactional
@IntegrationTestSupport
class InvitationServiceTest extends AbstractContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    FeedContributorRepository feedContributorRepository;

    @Autowired
    InvitationRepository invitationRepository;

    @Autowired
    InvitationService invitationService;

    @MockBean
    NotificationEventListener mockEventListener;

    @Test
    @DisplayName("피드 주인이 다른 사용자를 피드에 초대하면 사용자를 향한 초대가 생성된다")
    void inviteUserTest() throws Exception {
        //given
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when
        invitationService.inviteUser(owner.getId(), feedEntity.getId(), request);

        //then
        Assertions.assertThat(invitationRepository.findAll())
                .hasSize(1)
                .extracting("userToEntity", "status")
                .containsExactly(
                        tuple(invitedUser, InvitationStatus.COMPLETED)
                );
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
        Assertions.assertThatThrownBy(() -> invitationService.inviteUser(owner.getId(), notExistingFeedId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FEED_NOT_FOUND);
    }

    // TODO 사용자가 초대를 거절한 상태에서 다시 초대 요청 테스트

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
        Assertions.assertThatThrownBy(() -> invitationService.inviteUser(owner.getId(), feedEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }

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
        invitationService.inviteUser(owner.getId(), feedEntity.getId(), request);

        //then
        Assertions.assertThat(feedContributorRepository.findAll())
                .hasSize(1)
                .extracting("userEntity")
                .containsOnly(invitedUser);
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
        invitationService.inviteUser(owner.getId(), feedEntity.getId(), request);

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

        FeedInviteRequest request = FeedInviteRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> invitationService.inviteUser(other.getId(), feedEntity.getId(), request))
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
        Assertions.assertThatThrownBy(() -> invitationService.inviteUser(owner.getId(), feedEntity.getId(), request))
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
        invitationService.inviteUser(owner.getId(), feedEntity.getId(), request);

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

        feedContributorRepository.save(createManager(alreadyInvitedUser, feedEntity));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(alreadyInvitedUser.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> invitationService.inviteUser(owner.getId(), feedEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_ALREADY_INVITED);
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

    private FeedContributorEntity createManager(UserEntity userEntity, FeedEntity feedEntity) {
        return FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}