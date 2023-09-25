package world.trecord.service.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractConcurrencyTest;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class UserRecordLikeServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        userRecordLikeRepository.physicallyDeleteAll();
        recordRepository.physicallyDeleteAll();
        feedRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @DisplayName("사용자가 동일한 기록에 좋아요 요청을 동시에 요청해도 순서대로 처리된다")
    @CsvSource({"100,false", "101,true"})
    @ParameterizedTest
    void toggleLikeConcurrencyTest(int totalRequestCount, boolean expected) throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity other = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, other));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        List<Callable<Void>> tasks = generateConcurrentTasks(totalRequestCount, () -> {
            userRecordLikeService.toggleLike(other.getId(), recordEntity.getId());
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
                exceptionCount++;
            }
        }

        Assertions.assertThat(exceptionCount).isZero();
        Assertions.assertThat(userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(other.getId(), recordEntity.getId())).isEqualTo(expected);
    }
}