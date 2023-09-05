package world.trecord.service.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.record.request.RecordCreateRequest;
import world.trecord.service.record.response.RecordCreateResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@IntegrationTestSupport
class RecordServiceConcurrencyTest extends ContainerBaseTest {

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
    @DisplayName("같은 피드에 같은 날짜에 여러 요청을 동시에 수행해도 같은 번호를 가지지 않는다")
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

    private RecordCreateRequest createRequest(FeedEntity feedEntity) {
        return RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title("title")
                .date(LocalDateTime.of(2023, 1, 1, 0, 0))
                .place("jeju")
                .feeling("feeling")
                .weather("weather")
                .transportation("best")
                .content("content")
                .companion("companion")
                .build();
    }
}
