package world.trecord.service.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.feedcontributor.request.FeedInviteRequest;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractConcurrencyTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class FeedContributorServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        feedContributorRepository.deleteAll();
        feedRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 사용자에게 초대를 동시에 해도 초대는 한 번만 된다")
    void inviteUserConcurrencyTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity invitedUser = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        final int inviteRequestCount = 10;
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < inviteRequestCount; i++) {
            tasks.add(() -> {
                feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), request);
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
        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("피드 컨트리뷰터를 동시에 내보내도 내보내기는 한 번만 된다")
    void expelUserConcurrencyTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity invitedUser = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        final int inviteRequestCount = 10;
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < inviteRequestCount; i++) {
            tasks.add(() -> {
                feedContributorService.expelUserFromFeed(owner.getId(), invitedUser.getId(), feedEntity.getId());
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
                        (((CustomException) e.getCause()).error() == CustomExceptionError.USER_NOT_INVITED)) {
                    exceptionCount++;
                }
            }
        }

        Assertions.assertThat(exceptionCount).isEqualTo(inviteRequestCount - 1);
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();

        //finally
        executorService.shutdown();
    }

    @Test
    @DisplayName("피드에서 나가는 것을 동시에 요청해도 한 번만 처리된다")
    void leaveFeedConcurrencyTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity invitedUser = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        final int inviteRequestCount = 10;
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < inviteRequestCount; i++) {
            tasks.add(() -> {
                feedContributorService.leaveFeed(invitedUser.getId(), feedEntity.getId());
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
                        (((CustomException) e.getCause()).error() == CustomExceptionError.USER_NOT_INVITED)) {
                    exceptionCount++;
                }
            }
        }

        Assertions.assertThat(exceptionCount).isEqualTo(inviteRequestCount - 1);
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }
}