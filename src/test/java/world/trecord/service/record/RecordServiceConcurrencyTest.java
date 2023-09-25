package world.trecord.service.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordSequenceSwapRequest;
import world.trecord.dto.record.response.RecordCreateResponse;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractConcurrencyTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class RecordServiceConcurrencyTest extends AbstractConcurrencyTest {

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        recordRepository.physicallyDeleteAll();
        recordSequenceRepository.physicallyDeleteAll();
        feedRepository.physicallyDeleteAll();
        userRepository.physicallyDeleteAll();
    }

    @Test
    @DisplayName("같은 피드에 같은 날짜에 기록을 동시에 저장해도 같은 순서 번호를 가지지 않는다")
    void createRecordWithSequenceConcurrencyTest() throws InterruptedException {
        //given
        final int TOTAL_REQUEST_COUNT = 200;

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        List<Callable<RecordCreateResponse>> tasks = generateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            RecordCreateRequest recordCreateRequest = buildCreateRequest(feedEntity);
            return recordService.createRecord(userEntity.getId(), recordCreateRequest);
        });

        //when
        List<Future<RecordCreateResponse>> futures = executorService.invokeAll(tasks);

        //then
        int exceptionCount = 0;
        for (Future<RecordCreateResponse> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                exceptionCount++;
            }
        }

        Assertions.assertThat(exceptionCount).isZero();

        Assertions.assertThat(recordRepository.findAll())
                .extracting("sequence")
                .hasSize(TOTAL_REQUEST_COUNT)
                .doesNotHaveDuplicates();

        Assertions.assertThat(recordRepository.findMaxSequenceByFeedEntityIdAndDate(feedEntity.getId(), buildCreateRequest(feedEntity).getDate()))
                .isPresent()
                .hasValue(TOTAL_REQUEST_COUNT);
    }

    @Test
    @DisplayName("같은 날짜를 가진 기록들의 순서 변경을 동시에 해도 순서가 순서대로 변경된다")
    void swapSequenceConcurrencyTest() throws Exception {
        //given
        final int TOTAL_REQUEST_COUNT = 11;

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        int recordSequence1 = 1;
        int recordSequence2 = 2;
        RecordEntity recordEntity1 = RecordEntityFixture.of(userEntity, feedEntity, recordSequence1);
        RecordEntity recordEntity2 = RecordEntityFixture.of(userEntity, feedEntity, recordSequence2);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        RecordSequenceSwapRequest swapRequest = buildSwapRequest(recordEntity1, recordEntity2);

        List<Callable<Void>> tasks = generateConcurrentTasks(TOTAL_REQUEST_COUNT, () -> {
            recordService.swapRecordSequence(userEntity.getId(), swapRequest);
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
        Assertions.assertThat(recordRepository.findById(recordEntity1.getId()).get().getSequence()).isEqualTo(recordSequence2);
        Assertions.assertThat(recordRepository.findById(recordEntity2.getId()).get().getSequence()).isEqualTo(recordSequence1);
    }

    private RecordSequenceSwapRequest buildSwapRequest(RecordEntity recordEntity1, RecordEntity recordEntity2) {
        return RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();
    }

    private RecordCreateRequest buildCreateRequest(FeedEntity feedEntity) {
        return RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title("title")
                .date(LocalDateTime.of(2023, 1, 1, 0, 0))
                .place("jeju")
                .latitude("latitude")
                .longitude("longitude")
                .feeling("feeling")
                .weather("weather")
                .transportation("best")
                .content("content")
                .companion("companion")
                .build();
    }
}
