package world.trecord.domain.invitation;

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
class InvitationRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    InvitationRepository invitationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Test
    @DisplayName("피드 아이디로 soft delete all한다")
    void deleteAllByFeedEntityIdTest() throws Exception {
        //given
        UserEntity userEntity1 = createUser("test1@email.com");
        UserEntity userEntity2 = createUser("test2@email.com");
        UserEntity userEntity3 = createUser("test3@email.com");
        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity1));

        InvitationEntity invitation1 = createInvitation(userEntity2, feedEntity);
        InvitationEntity invitation2 = createInvitation(userEntity3, feedEntity);
        invitationRepository.saveAll(List.of(invitation1, invitation2));

        //when
        invitationRepository.deleteAllByFeedEntityId(feedEntity.getId());

        //then
        Assertions.assertThat(invitationRepository.findAll()).isEmpty();
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
                .startAt(LocalDateTime.of(2023, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2023, 3, 10, 0, 0))
                .build();
    }

    private InvitationEntity createInvitation(UserEntity userEntity, FeedEntity feedEntity) {
        return InvitationEntity.builder()
                .userToEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}