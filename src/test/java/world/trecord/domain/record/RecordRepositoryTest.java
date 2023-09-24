package world.trecord.domain.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feed.Place;
import world.trecord.domain.record.projection.RecordWithFeedProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@IntegrationTestSupport
class RecordRepositoryTest extends AbstractContainerBaseTest {

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
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity record1 = createRecord(feedEntity, 0);
        RecordEntity record2 = createRecord(feedEntity, 1);
        RecordEntity record3 = createRecord(feedEntity, 2);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordsByFeedEntityId(feedEntity.getId());

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
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity record1 = createRecord(feedEntity, 2);
        RecordEntity record2 = createRecord(feedEntity, 1);
        RecordEntity record3 = createRecord(feedEntity, 0);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordsByFeedEntityId(feedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(3)
                .extracting("id")
                .containsExactly(record3.getId(), record2.getId(), record1.getId());
    }

    @Test
    @DisplayName("Select for update 쿼리가 날라가야한다")
    void findByIdForUpdateTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        RecordEntity lockedRecord = recordRepository.findByIdForUpdate(recordEntity.getId()).orElse(null);

        //then
        Assertions.assertThat(lockedRecord).isNotNull();
    }

    @Test
    @DisplayName("피드 아이디로 기록 리스트를 기록 날짜, 기록 순서가 동일하면 기록 등록 시간 오름차순으로 projection으로 조회한다")
    void findRecordEntityByFeedIdOrderByCreatedTimeTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity record1 = createRecord(feedEntity, 1);
        RecordEntity record2 = createRecord(feedEntity, 2);
        RecordEntity record3 = createRecord(feedEntity, 3);
        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordsByFeedEntityId(feedEntity.getId());

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
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        Optional<RecordEntity> optionalRecord = recordRepository.findWithFeedEntityById(recordEntity.getId());

        //then
        Assertions.assertThat(optionalRecord).isPresent();
    }

    @Test
    @DisplayName("피드에 등록된 같은 날짜에 있는 기록 중 가장 큰 순서 번호를 조회한다")
    void findMaxSequenceByFeedAndDateTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        int sequence = 100;
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, sequence));

        //when
        Optional<Integer> maxSequence = recordRepository.findMaxSequenceByFeedEntityIdAndDate(feedEntity.getId(), recordEntity.getDate());

        //then
        Assertions.assertThat(maxSequence.orElse(0)).isEqualTo(sequence);
    }

    @Test
    @DisplayName("피드에 등록된 기록 리스트를 pagination으로 조회한다")
    void findRecordListByFeedEntityIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity, 1));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity, 2));
        RecordEntity recordEntity3 = recordRepository.save(createRecord(feedEntity, 3));
        RecordEntity recordEntity4 = recordRepository.save(createRecord(feedEntity, 4));
        RecordEntity recordEntity5 = recordRepository.save(createRecord(feedEntity, 5));
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4, recordEntity5));

        PageRequest page = PageRequest.of(0, 2);

        //when
        Page<RecordWithFeedProjection> result = recordRepository.findRecordListByFeedEntityId(feedEntity.getId(), page);

        //then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("기록 아이디 리스트에 포함된 기록들을 write lock으로 조회한다")
    void findByIdsForUpdate() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity1 = createRecord(feedEntity, 0);
        RecordEntity recordEntity2 = createRecord(feedEntity, 1);
        RecordEntity recordEntity3 = createRecord(feedEntity, 2);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        List<RecordEntity> result = recordRepository.findByIdsForUpdate(List.of(recordEntity1.getId(), recordEntity2.getId(), recordEntity3.getId()));

        //then
        Assertions.assertThat(result)
                .hasSize(3)
                .containsAll(List.of(recordEntity1, recordEntity2, recordEntity3));
    }

    @Test
    @DisplayName("피드에 등록된 같은 날짜에 있는 기록이 없으면 0을 반환한다")
    void findMaxSequenceByFeedAndDateWhenSameDateIsEmptyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        LocalDateTime date = LocalDateTime.of(2022, 3, 3, 0, 0);

        //when
        Optional<Integer> maxSequence = recordRepository.findMaxSequenceByFeedEntityIdAndDate(feedEntity.getId(), date);

        //then
        Assertions.assertThat(maxSequence).isEmpty();
    }

    @Test
    @DisplayName("피드에 등록된 기록을 모두 soft delete한다")
    void deleteAllByFeedEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity record1 = createRecord(feedEntity, 0);
        RecordEntity record2 = createRecord(feedEntity, 1);
        RecordEntity record3 = createRecord(feedEntity, 2);

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        recordRepository.deleteAllByFeedEntityId(feedEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("피드 아이디와 작성자 아이디로 soft delete한다")
    void deleteByFeedEntityIdAndUserEntityIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        recordRepository.deleteByFeedEntityIdAndUserEntityId(feedEntity.getId(), userEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("기록을 soft delete한다")
    void deleteTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        recordRepository.delete(recordEntity);

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email("test@email.com")
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .startAt(LocalDateTime.of(2023, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2023, 3, 31, 0, 0))
                .userEntity(userEntity)
                .name("name")
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("title")
                .place(Place.of("place", "0", "0"))
                .date(LocalDateTime.of(2022, 3, 1, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }
}