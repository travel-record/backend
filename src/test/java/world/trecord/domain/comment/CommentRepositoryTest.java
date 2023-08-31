package world.trecord.domain.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class CommentRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Test
    @DisplayName("사용자가 작성한 댓글 리스트를 등록 시간 내림차순으로 기록과 함께 조회하여 projection으로 반환한다")
    void findByUserEntityOrderByCreatedDateTimeDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity1 = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        RecordEntity recordEntity2 = recordRepository.save(createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        String content4 = "content4";

        CommentEntity commentEntity1 = createCommentEntity(userEntity, recordEntity1, null, content1);
        CommentEntity commentEntity2 = createCommentEntity(userEntity, recordEntity2, null, content2);
        CommentEntity commentEntity3 = createCommentEntity(userEntity, recordEntity2, null, content3);
        CommentEntity commentEntity4 = createCommentEntity(userEntity, recordEntity1, null, content4);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when
        List<CommentRecordProjection> projectionList = commentRepository.findByUserEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        //then
        Assertions.assertThat(projectionList)
                .hasSize(4)
                .extracting("recordId", "commentId")
                .containsExactly(
                        tuple(recordEntity1.getId(), commentEntity4.getId()),
                        tuple(recordEntity2.getId(), commentEntity3.getId()),
                        tuple(recordEntity2.getId(), commentEntity2.getId()),
                        tuple(recordEntity1.getId(), commentEntity1.getId()));
    }

    @Test
    @DisplayName("사용자가 작성한 댓글이 없으면 빈 배열을 반환한다")
    void findByUserEntityOrderByCreatedDateTimeDescWhenUserNotCommentOnRecordTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        //when
        List<CommentRecordProjection> projectionList = commentRepository.findByUserEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        //then
        Assertions.assertThat(projectionList).isEmpty();
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트를 등록 시간 오름차순으로 조회한다")
    void findCommentEntityByRecordEntityOrderByCreatedDateTimeAsc() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        String content4 = "content4";

        CommentEntity commentEntity1 = createCommentEntity(userEntity, recordEntity, null, content1);
        CommentEntity commentEntity2 = createCommentEntity(userEntity, recordEntity, null, content2);
        CommentEntity commentEntity3 = createCommentEntity(userEntity, recordEntity, null, content3);
        CommentEntity commentEntity4 = createCommentEntity(userEntity, recordEntity, null, content4);

        commentRepository.saveAll(List.of(commentEntity4, commentEntity3, commentEntity2, commentEntity1));

        //when
        List<CommentEntity> commentEntities = commentRepository.findCommentEntityByRecordEntityIdOrderByCreatedDateTimeAsc(recordEntity.getId());

        //then
        Assertions.assertThat(commentEntities)
                .hasSize(4)
                .extracting("content")
                .containsExactly(content4, content3, content2, content1);
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트가 없으면 빈 배열을 반환한다")
    void findCommentEntityByRecordEntityOrderByCreatedDateTimeAscReturnsEmptyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        List<CommentEntity> commentEntities = commentRepository.findCommentEntityByRecordEntityIdOrderByCreatedDateTimeAsc(recordEntity.getId());

        //then
        Assertions.assertThat(commentEntities).isEmpty();
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트를 soft delete한다")
    void deleteAllByRecordEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(userEntity, recordEntity, null, "content1");
        CommentEntity commentEntity2 = createCommentEntity(userEntity, recordEntity, null, "content2");
        CommentEntity commentEntity3 = createCommentEntity(userEntity, recordEntity, null, "content3");
        CommentEntity commentEntity4 = createCommentEntity(userEntity, recordEntity, null, "content4");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when
        commentRepository.deleteAllByRecordEntityId(recordEntity.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("원댓글로 대댓글 리스트를 soft delete한다")
    void deleteAllByCommentEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity parentComment = createCommentEntity(userEntity, recordEntity, null, "content1");
        commentRepository.save(parentComment);

        CommentEntity commentEntity1 = createCommentEntity(userEntity, recordEntity, parentComment, "content2");
        CommentEntity commentEntity2 = createCommentEntity(userEntity, recordEntity, parentComment, "content3");
        CommentEntity commentEntity3 = createCommentEntity(userEntity, recordEntity, parentComment, "content4");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        //when
        commentRepository.deleteAllByCommentEntityId(parentComment.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).containsOnly(parentComment);
    }

    @Test
    @DisplayName("댓글을 soft delete한다")
    void softDeleteTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity = createCommentEntity(userEntity, recordEntity, null, "content1");
        commentRepository.save(commentEntity);

        //when
        commentRepository.softDeleteById(commentEntity.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, String title, String place, LocalDateTime date, String content, String weather, String satisfaction, String feeling) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(title)
                .place(place)
                .date(date)
                .content(content)
                .weather(weather)
                .transportation(satisfaction)
                .feeling(feeling)
                .build();
    }

    private CommentEntity createCommentEntity(UserEntity userEntity, RecordEntity recordEntity, CommentEntity parentCommentEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .parentCommentEntity(parentCommentEntity)
                .content(content)
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