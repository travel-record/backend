package world.trecord.service.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractConcurrencyTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class UserRecordLikeServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        userRecordLikeRepository.deleteAll();
        recordRepository.deleteAll();
        feedRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자가 동일한 기록에 좋아요 요청을 동시에 해도 동시성을 제어한다")
    void toggleLikeConcurrencyTest() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity other = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, other));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        final int NUMBER_OF_REQUESTS = 11;
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            tasks.add(() -> {
                userRecordLikeService.toggleLike(other.getId(), recordEntity.getId());
                return null;
            });
        }

        //when
        List<Future<Void>> futures = executorService.invokeAll(tasks);

        //then
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
            }
        }

        Assertions.assertThat(userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(other.getId(), recordEntity.getId())).isTrue();

        //finally
        executorService.shutdown();
    }
}