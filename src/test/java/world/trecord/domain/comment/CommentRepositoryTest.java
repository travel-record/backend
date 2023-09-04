package world.trecord.domain.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@Transactional
@IntegrationTestSupport
class CommentRepositoryTest extends ContainerBaseTest {

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
        UserEntity userEntity = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = createRecord(feedEntity, 1);
        RecordEntity recordEntity2 = createRecord(feedEntity, 2);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        CommentEntity commentEntity1 = createComment(userEntity, recordEntity1, null);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity2, null);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity2, null);
        CommentEntity commentEntity4 = createComment(userEntity, recordEntity1, null);
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
        UserEntity userEntity = userRepository.save(createUser());

        //when
        List<CommentRecordProjection> projectionList = commentRepository.findByUserEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        //then
        Assertions.assertThat(projectionList).isEmpty();
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트를 등록 시간 오름차순으로 조회한다")
    void findCommentEntityByRecordEntityOrderByCreatedDateTimeAsc() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        CommentEntity commentEntity1 = createComment(userEntity, recordEntity, null);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity, null);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity, null);
        CommentEntity commentEntity4 = createComment(userEntity, recordEntity, null);

        commentRepository.saveAll(List.of(commentEntity4, commentEntity3, commentEntity2, commentEntity1));

        //when
        List<CommentEntity> commentEntities = commentRepository.findWithUserEntityByRecordEntityIdOrderByCreatedDateTimeAsc(recordEntity.getId());

        //then
        Assertions.assertThat(commentEntities)
                .hasSize(4)
                .extracting("id")
                .containsExactly(commentEntity4.getId(), commentEntity3.getId(), commentEntity2.getId(), commentEntity1.getId());
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트가 없으면 빈 배열을 반환한다")
    void findCommentEntityByRecordEntityOrderByCreatedDateTimeAscReturnsEmptyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        //when
        List<CommentEntity> commentEntities = commentRepository.findWithUserEntityByRecordEntityIdOrderByCreatedDateTimeAsc(recordEntity.getId());

        //then
        Assertions.assertThat(commentEntities).isEmpty();
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트를 soft delete한다")
    void deleteAllByRecordEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        CommentEntity commentEntity1 = createComment(userEntity, recordEntity, null);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity, null);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity, null);
        CommentEntity commentEntity4 = createComment(userEntity, recordEntity, null);

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
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        CommentEntity parentComment = createComment(userEntity, recordEntity, null);

        commentRepository.save(parentComment);

        CommentEntity commentEntity1 = createComment(userEntity, recordEntity, parentComment);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity, parentComment);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity, parentComment);

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
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 1));

        CommentEntity commentEntity = createComment(userEntity, recordEntity, null);

        commentRepository.save(commentEntity);

        //when
        commentRepository.softDeleteById(commentEntity.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email("test@email.com")
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2023, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2023, 3, 31, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("title")
                .place("place")
                .date(LocalDateTime.of(2023, 3, 1, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity, CommentEntity parentCommentEntity) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .parentCommentEntity(parentCommentEntity)
                .content("content")
                .build();
    }
}