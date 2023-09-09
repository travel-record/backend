package world.trecord.service.invitation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.invitation.InvitationRepository;
import world.trecord.domain.manager.ManagerRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEventListener;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.invitation.request.FeedInviteRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@IntegrationTestSupport
class InvitationServiceConcurrencyTest extends AbstractContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    InvitationRepository invitationRepository;

    @Autowired
    InvitationService invitationService;

    @MockBean
    NotificationEventListener mockEventListener;

    @AfterEach
    void tearDown() {
        invitationRepository.deleteAllInBatch();
        managerRepository.deleteAllInBatch();
        feedRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("같은 사용자에게 초대를 동시에 해도 초대는 한 번만 된다")
    void inviteConcurrencyTest() throws Exception {
        //given
        UserEntity owner = createUser("owner@email.com");
        UserEntity invitedUser = createUser("invited@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        final int inviteRequestCount = 10;
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < inviteRequestCount; i++) {
            tasks.add(() -> {
                invitationService.inviteUser(owner.getId(), feedEntity.getId(), request);
                return null;
            });
        }

        //when
        List<Future<Void>> futures = executorService.invokeAll(tasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof CustomException &&
                        (((CustomException) e.getCause()).error() == CustomExceptionError.USER_ALREADY_INVITED)) {
                    exceptionCount++;
                }
            }
        }

        Assertions.assertThat(exceptionCount).isEqualTo(inviteRequestCount - 1);
        Assertions.assertThat(managerRepository.findAll()).hasSize(1);
        Assertions.assertThat(invitationRepository.findAll()).hasSize(1);

        executorService.shutdown();
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
}