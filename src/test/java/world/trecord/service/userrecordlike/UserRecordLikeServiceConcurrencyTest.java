package world.trecord.service.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@IntegrationTestSupport
class UserRecordLikeServiceConcurrencyTest extends AbstractContainerBaseTest {

    @Autowired
    UserRecordLikeService userRecordLikeService;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

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
        UserEntity owner = createUser();
        UserEntity other = createUser();
        userRepository.saveAll(List.of(owner, other));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

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

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 9, 30, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }
}