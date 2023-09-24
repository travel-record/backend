package world.trecord.service.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordSequenceSwapRequest;
import world.trecord.dto.record.response.RecordCreateResponse;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@IntegrationTestSupport
class RecordServiceConcurrencyTest extends AbstractContainerBaseTest {

    @Autowired
    RecordService recordService;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    RecordSequenceRepository recordSequenceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @AfterEach
    void tearDown() {
        recordRepository.deleteAllInBatch();
        recordSequenceRepository.deleteAllInBatch();
        feedRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("같은 피드에 같은 날짜에 여러 기록을 동시에 저장해도 같은 번호를 가지지 않는다")
    void createRecordWithSequenceConcurrencyTest() throws InterruptedException {
        //given
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);
        final int NUMBER_OF_REQUESTS = 200;

        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        CountDownLatch latch = new CountDownLatch(NUMBER_OF_REQUESTS);
        List<Future<RecordCreateResponse>> futures = new ArrayList<>();

        //when
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            RecordCreateRequest request = createRequest(feedEntity);

            futures.add(executorService.submit(() -> {
                try {
                    return recordService.createRecord(userEntity.getId(), request);
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(1, TimeUnit.SECONDS);

        for (Future<RecordCreateResponse> future : futures) {
            try {
                assertNotNull(future.get());
            } catch (ExecutionException e) {
            }
        }

        //then
        Assertions.assertThat(recordRepository.findAll())
                .extracting("sequence")
                .hasSize(NUMBER_OF_REQUESTS)
                .doesNotHaveDuplicates();

        Assertions.assertThat(recordRepository.findMaxSequenceByFeedEntityIdAndDate(feedEntity.getId(), createRequest(feedEntity).getDate()))
                .isPresent()
                .hasValue(NUMBER_OF_REQUESTS);

        //finally
        executorService.shutdown();
    }

    @Test
    @DisplayName("같은 날짜를 가진 기록들의 순서 변경 요청을 동시에 해도 순서가 정상적으로 변경된다")
    void swapSequenceConcurrencyTest() throws Exception {
        //given
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfCores);

        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        int recordSeq1 = 1;
        RecordEntity recordEntity1 = createRecord(feedEntity, recordSeq1);
        int recordSeq2 = 2;
        RecordEntity recordEntity2 = createRecord(feedEntity, recordSeq2);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        final int NUMBER_OF_REQUESTS = 11;

        List<Callable<Void>> tasks = new ArrayList<>();

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            tasks.add(() -> {
                recordService.swapRecordSequence(userEntity.getId(), request);
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

        Assertions.assertThat(recordRepository.findById(recordEntity1.getId()))
                .isPresent()
                .hasValueSatisfying(
                        entity -> {
                            Assertions.assertThat(entity.getSequence()).isEqualTo(recordSeq2);
                        }
                );

        Assertions.assertThat(recordRepository.findById(recordEntity2.getId()))
                .isPresent()
                .hasValueSatisfying(
                        entity -> {
                            Assertions.assertThat(entity.getSequence()).isEqualTo(recordSeq1);
                        }
                );

        //finally
        executorService.shutdown();
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email(UUID.randomUUID().toString() + System.currentTimeMillis() + Thread.currentThread().getId())
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2022, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .longitude("longitude")
                .latitude("latitude")
                .date(LocalDateTime.of(2022, 10, 1, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }

    private RecordCreateRequest createRequest(FeedEntity feedEntity) {
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
