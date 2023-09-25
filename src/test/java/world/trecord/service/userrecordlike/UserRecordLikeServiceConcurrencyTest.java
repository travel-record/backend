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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

class UserRecordLikeServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        userRecordLikeRepository.physicallyDeleteAll();
        recordRepository.physicallyDeleteAll();
        feedRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("사용자가 동일한 기록에 좋아요 요청을 동시에 해도 순서대로 실행된다")
    void toggleLikeConcurrencyTest() throws Exception {
        //given
        final int NUMBER_OF_REQUESTS = 10;
        List<Callable<Void>> tasks = new ArrayList<>();

        UserEntity owner = UserEntityFixture.of();
        UserEntity other = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, other));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        IntStream.rangeClosed(0, NUMBER_OF_REQUESTS).forEach(request -> {
            tasks.add(() -> {
                userRecordLikeService.toggleLike(other.getId(), recordEntity.getId());
                return null;
            });
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
        Assertions.assertThat(userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(other.getId(), recordEntity.getId())).isTrue();
    }
}