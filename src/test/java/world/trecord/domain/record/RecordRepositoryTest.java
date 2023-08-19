package world.trecord.domain.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.projection.RecordWithFeedProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    @DisplayName("기록 상세 정보를 등록 시간 오름차순으로 정렬된 댓글 리스트와 사용자 정보를 함께 조회한다")
    void findWithFeedEntityAndCommentEntitiesByTest() throws Exception {
        //given
        UserEntity savedUserEntity1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity savedUserEntity2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity savedFeedEntity = feedRepository.save(createFeedEntity(savedUserEntity1, "feed name"));
        RecordEntity savedRecordEntity = recordRepository.save(createRecordEntity(savedFeedEntity, "record", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content", "weather", "satisfaction", "feeling"));
        CommentEntity commentEntity1 = createCommentEntity(savedUserEntity1, savedRecordEntity);
        CommentEntity commentEntity2 = createCommentEntity(savedUserEntity2, savedRecordEntity);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordEntity recordEntity = recordRepository.findRecordEntityWithFeedEntityAndCommentEntitiesBy(savedRecordEntity.getId()).get();

        //then
        Assertions.assertThat(recordEntity).isEqualTo(savedRecordEntity);
        Assertions.assertThat(recordEntity.getCommentEntities()).containsExactly(commentEntity1, commentEntity2);
        Assertions.assertThat(recordEntity.getFeedEntity()).isEqualTo(savedFeedEntity);
        Assertions.assertThat(recordEntity.getCommentEntities().stream().map(CommentEntity::getUserEntity).collect(Collectors.toList()))
                .contains(savedUserEntity1, savedUserEntity2);
    }

    @Test
    @DisplayName("피드 아이디로 기록 리스트를 기록 날짜,기록 등록 날짜 오름차순으로 projection으로 조회한다")
    void findRecordEntityByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name"));
        RecordEntity record1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling");
        RecordEntity record2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content", "weather", "satisfaction", "feeling");
        RecordEntity record3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling");

        recordRepository.saveAll(List.of(record1, record2, record3));

        //when
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordEntityByFeedId(feedEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(3)
                .extracting("title")
                .containsExactly(record1.getTitle(), record3.getTitle(), record2.getTitle());
    }

    @Test
    @DisplayName("기록과 기록 하위 댓글들을 함께 조회한다")
    void findRecordEntityWithCommentEntitiesByIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name"));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 1, 0, 0), "content", "weather", "satisfaction", "feeling"));

        CommentEntity commentEntity1 = createCommentEntity(userEntity, recordEntity);
        CommentEntity commentEntity2 = createCommentEntity(userEntity, recordEntity);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordEntity foundRecordEntity = recordRepository.findRecordEntityWithCommentEntitiesById(recordEntity.getId()).get();

        //then
        Assertions.assertThat(foundRecordEntity.getCommentEntities())
                .hasSize(2)
                .containsOnly(commentEntity1, commentEntity2);
    }

    private CommentEntity createCommentEntity(UserEntity savedUserEntity, RecordEntity savedRecordEntity) {
        return CommentEntity.builder()
                .recordEntity(savedRecordEntity)
                .content("content1")
                .userEntity(savedUserEntity)
                .build();
    }


    private FeedEntity createFeedEntity(UserEntity userEntity, String name) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name(name)
                .build();
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
}