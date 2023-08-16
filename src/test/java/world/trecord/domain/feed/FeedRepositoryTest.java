package world.trecord.domain.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.feed.projection.FeedWithRecordProjection;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class FeedRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @DisplayName("사용자가 등록한 피드 리스트를 여행 시작 시간 내림차순으로 조회한다")
    void findByUserEntityOrderByStartAtDescTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity1 = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeedEntity(saveUserEntity, "feed name2", LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeedEntity(saveUserEntity, "feed name3", LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));

        //when
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityOrderByStartAtDesc(userEntity);

        //then
        Assertions.assertThat(feedEntities).extracting("name")
                .containsExactly(
                        "feed name3", "feed name2", "feed name1"
                );
    }

    @Test
    @DisplayName("피드와 피드에 등록된 기록과 함께 조회한다")
    void findFeedEntityWithRecordEntitiesByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        FeedEntity foundFeedEntity = feedRepository.findFeedEntityWithRecordEntitiesById(savedFeedEntity.getId()).get();

        //then
        Assertions.assertThat(foundFeedEntity).isEqualTo(savedFeedEntity);
        Assertions.assertThat(foundFeedEntity.getRecordEntities())
                .contains(recordEntity1, recordEntity2, recordEntity3);
    }

    @Test
    @DisplayName("등록된 기록이 없는 피드를 조회하면 기록은 빈 리스트로 반환된다")
    void findFeedEntityWithEmptyRecordEntitiesByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        //when
        FeedEntity foundFeedEntity = feedRepository.findFeedEntityWithRecordEntitiesById(savedFeedEntity.getId()).get();

        //then
        Assertions.assertThat(foundFeedEntity).isEqualTo(savedFeedEntity);
        Assertions.assertThat(foundFeedEntity.getRecordEntities()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("피드를 삭제한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        //when
        feedRepository.delete(savedFeedEntity);

        //then
        Assertions.assertThat(feedRepository.findById(savedFeedEntity.getId())).isEmpty();
    }


    @Test
    @DisplayName("기록이 등록된 피드를 기록과 함께 조회하여 기록이 시간순으로 정렬된 Projection List로 반환한다")
    void findFeedProjectionWithRecordProjectionByIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        List<FeedWithRecordProjection> projectionList = feedRepository.findWithUserEntityAndRecordEntitiesBy(savedFeedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .extracting("writerId", "feedId", "recordId")
                .hasSize(3)
                .containsExactly(
                        tuple(userEntity.getId(), feedEntity.getId(), recordEntity2.getId()),
                        tuple(userEntity.getId(), feedEntity.getId(), recordEntity1.getId()),
                        tuple(userEntity.getId(), feedEntity.getId(), recordEntity3.getId())
                );
    }

    @Test
    @DisplayName("기록이 등록되지 않은 피드를 기록과 함께 조회하면 Record 필드는 null로 반환한다")
    void findFeedProjectionWithRecordProjectionWithEmptyRecordsByIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        //when
        List<FeedWithRecordProjection> projectionList = feedRepository.findWithUserEntityAndRecordEntitiesBy(savedFeedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .extracting("writerId", "feedId", "recordId")
                .hasSize(1)
                .containsExactly(
                        tuple(userEntity.getId(), feedEntity.getId(), null)
                );
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, String record, String place, LocalDateTime date, String content, String weather, String satisfaction, String feeling) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(record)
                .place(place)
                .date(date)
                .content(content)
                .weather(weather)
                .transportation(satisfaction)
                .feeling(feeling)
                .build();
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }
}