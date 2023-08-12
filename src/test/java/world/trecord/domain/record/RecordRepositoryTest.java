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
    @DisplayName("기록 상세 정보를 정렬된 댓글 리스트와 사용자 정보를 함께 조회한다")
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
        RecordEntity recordEntity = recordRepository.findWithFeedEntityAndCommentEntitiesBy(savedRecordEntity.getId()).get();

        //then
        Assertions.assertThat(recordEntity).isEqualTo(savedRecordEntity);
        Assertions.assertThat(recordEntity.getCommentEntities()).containsExactly(commentEntity1, commentEntity2);
        Assertions.assertThat(recordEntity.getFeedEntity()).isEqualTo(savedFeedEntity);
        Assertions.assertThat(recordEntity.getCommentEntities().stream().map(CommentEntity::getUserEntity).collect(Collectors.toList()))
                .contains(savedUserEntity1, savedUserEntity2);

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