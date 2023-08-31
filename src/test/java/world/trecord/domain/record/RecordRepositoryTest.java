package world.trecord.domain.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.projection.RecordWithFeedProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@IntegrationTestSupport
class RecordRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    @DisplayName("피드 아이디로 기록 리스트를 기록 날짜 오름차순으로 projection으로 조회한다")
    void findRecordEntityByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2022, 3, 1, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2022, 3, 5, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt, "feed name"));
        RecordEntity record1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0);
        RecordEntity record2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content", "weather", "satisfaction", "feeling", 1);
        RecordEntity record3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content", "weather", "satisfaction", "feeling", 2);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordEntityByFeedId(feedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(3)
                .extracting("id")
                .containsExactly(record1.getId(), record2.getId(), record3.getId());
    }

    @Test
    @DisplayName("피드 아이디로 기록 리스트를 기록 날짜가 동일하면 기록 순서 오름차순으로 projection으로 조회한다")
    void findRecordEntityByFeedIdOrderBySequenceTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2022, 3, 1, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2022, 3, 5, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt, "feed name"));
        RecordEntity record1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 2);
        RecordEntity record2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 1);
        RecordEntity record3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordEntityByFeedId(feedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(3)
                .extracting("id")
                .containsExactly(record3.getId(), record2.getId(), record1.getId());
    }

    @Test
    @DisplayName("피드 아이디로 기록 리스트를 기록 날짜, 기록 순서가 동일하면 기록 등록 시간 오름차순으로 projection으로 조회한다")
    void findRecordEntityByFeedIdOrderByCreatedTimeTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2022, 3, 1, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2022, 3, 5, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt, "feed name"));
        RecordEntity record1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0);
        RecordEntity record2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0);
        RecordEntity record3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordEntityByFeedId(feedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(3)
                .extracting("id")
                .containsExactly(record1.getId(), record2.getId(), record3.getId());
    }

    @Test
    @DisplayName("기록을 조회할 때 피드와 함께 조회한다")
    void findRecordEntityWithFeedEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2022, 3, 1, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2022, 3, 5, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt, "feed name"));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0));

        //when
        Optional<RecordEntity> optionalRecord = recordRepository.findRecordEntityWithFeedEntityById(recordEntity.getId());

        //then
        RecordEntity record = optionalRecord.get();
        Assertions.assertThat(record.getFeedEntity()).isNotNull();
    }

    @Test
    @DisplayName("피드에 등록된 같은 날짜에 있는 기록 중 가장 큰 순서 번호를 조회한다")
    void findMaxSequenceByFeedAndDateTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, LocalDateTime.of(2022, 3, 1, 0, 0), LocalDateTime.of(2022, 3, 5, 0, 0), "feed name"));

        LocalDateTime date = LocalDateTime.of(2022, 3, 3, 0, 0);
        int sequence = 100;
        recordRepository.save(createRecordEntity(feedEntity, "title", "place", date, "content", "weather", "satisfaction", "feeling", sequence));

        //when
        Optional<Integer> maxSequence = recordRepository.findMaxSequenceByFeedIdAndDate(feedEntity.getId(), date);

        //then
        Assertions.assertThat(maxSequence.orElse(0)).isEqualTo(sequence);
    }

    @Test
    @DisplayName("피드에 등록된 같은 날짜에 있는 기록이 없으면 0을 반환한다")
    void findMaxSequenceByFeedAndDateWhenSameDateIsEmptyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, LocalDateTime.of(2022, 3, 1, 0, 0), LocalDateTime.of(2022, 3, 5, 0, 0), "feed name"));

        LocalDateTime date = LocalDateTime.of(2022, 3, 3, 0, 0);

        //when
        Optional<Integer> maxSequence = recordRepository.findMaxSequenceByFeedIdAndDate(feedEntity.getId(), date);

        //then
        Assertions.assertThat(maxSequence).isEmpty();
    }

    @Test
    @DisplayName("피드에 등록된 기록을 모두 soft delete한다")
    void deleteAllByFeedEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, LocalDateTime.of(2022, 3, 1, 0, 0), LocalDateTime.of(2022, 3, 5, 0, 0), "feed name"));

        RecordEntity record1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0);
        RecordEntity record2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content", "weather", "satisfaction", "feeling", 1);
        RecordEntity record3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content", "weather", "satisfaction", "feeling", 2);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        recordRepository.deleteAllByFeedEntityId(feedEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("기록을 soft delete한다")
    void deleteTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, LocalDateTime.of(2022, 3, 1, 0, 0), LocalDateTime.of(2022, 3, 5, 0, 0), "feed name"));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling", 0));

        //when
        recordRepository.softDeleteById(recordEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    private FeedEntity createFeedEntity(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt, String name) {
        return FeedEntity.builder()
                .startAt(startAt)
                .endAt(endAt)
                .userEntity(userEntity)
                .name(name)
                .build();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, String title, String place, LocalDateTime date, String content, String weather, String satisfaction, String feeling, int sequence) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(title)
                .place(place)
                .date(date)
                .content(content)
                .weather(weather)
                .transportation(satisfaction)
                .feeling(feeling)
                .sequence(sequence)
                .build();
    }
}