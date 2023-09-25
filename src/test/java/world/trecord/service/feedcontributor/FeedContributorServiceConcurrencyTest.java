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
import world.trecord.utils.ClassUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class FeedContributorServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        feedContributorRepository.physicallyDeleteAll();
        feedRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("피드에 동일한 사용자에게 초대를 동시에 해도 초대는 한 번만 한다")
    void inviteUserConcurrencyTest() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;

        UserEntity owner = UserEntityFixture.of();
        UserEntity invitedUser = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedInviteRequest inviteRequest = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        List<Callable<Void>> tasks = generateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            feedContributorService.inviteUserToFeed(owner.getId(), feedEntity.getId(), inviteRequest);
            return null;
        });

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

        Assertions.assertThat(exceptionCount).isEqualTo(TOTAL_REQUEST_COUNT - 1);
        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("동일한 피드 컨트리뷰터를 동시에 내보내기 요청해도 한 번만 처리한다")
    void expelUserConcurrencyTest() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;

        UserEntity owner = UserEntityFixture.of();
        UserEntity invitedUser = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        List<Callable<Void>> tasks = generateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            feedContributorService.expelUserFromFeed(owner.getId(), invitedUser.getId(), feedEntity.getId());
            return null;
        });

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

        Assertions.assertThat(exceptionCount).isEqualTo(TOTAL_REQUEST_COUNT - 1);
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 피드에서 나가는 것을 동시에 요청해도 한 번만 처리된다")
    void leaveFeedConcurrencyTest() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 10;

        UserEntity owner = UserEntityFixture.of();
        UserEntity invitedUser = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, invitedUser));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        List<Callable<Void>> tasks = generateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            feedContributorService.leaveFeed(invitedUser.getId(), feedEntity.getId());
            return null;
        });


        //when
        List<Future<Void>> futures = executorService.invokeAll(tasks);

        //then
        int exceptionCount = 0;
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof CustomException) {
                    CustomException customException = ClassUtils.getSafeInstance(e.getCause(), CustomException.class);
                    if (customException.error() == CustomExceptionError.USER_NOT_INVITED) {
                        exceptionCount++;
                    }
                }
            }
        }

        Assertions.assertThat(exceptionCount).isEqualTo(TOTAL_REQUEST_COUNT - 1);
        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }
}