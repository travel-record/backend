package world.trecord.web.service.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentUpdateResponse;

import java.time.LocalDateTime;
import java.util.List;

@IntegrationTestSupport
class CommentServiceTest extends ContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    CommentService commentService;

    @Test
    @DisplayName("사용자가 기록에 댓글을 작성하면 댓글 상세 정보를 반환한다")
    void createCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0)));

        String content = "content";

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(content)
                .build();

        //when
        commentService.createComment(userEntity.getId(), request);

        //then
        Assertions.assertThat(commentRepository.findAll())
                .hasSize(1)
                .extracting("content")
                .containsExactly(content);
    }

    @Test
    @DisplayName("대댓글을 작성하여 생성된 댓글 상세 정보를 반환한다")
    void createChildCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0)));

        CommentEntity parentCommentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .parentId(parentCommentEntity.getId())
                .content("content")
                .build();

        //when
        commentService.createComment(userEntity.getId(), request);

        //then
        Assertions.assertThat(commentRepository.findAll())
                .hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 기록에 댓글을 달려고 하면 예외가 발생한다")
    void createCommentWithNotExistingUserIdTest() throws Exception {
        //given
        long notExistingUserId = 0L;

        CommentCreateRequest request = CommentCreateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(notExistingUserId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 기록에 댓글을 달려고 하면 예외가 발생한다")
    void createCommentWithNotExistingRecordIdTest() throws Exception {
        //given
        long notExistingRecordId = 0L;

        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(notExistingRecordId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(userEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자가 댓글을 수정하면 수정된 댓글 내용을 반환한다")
    void updateCommentTest() throws Exception {
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0)));

        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        String changedContent = "changed content";

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(changedContent)
                .build();

        //when
        CommentUpdateResponse response = commentService.updateComment(userEntity.getId(), commentEntity.getId(), request);

        //then
        Assertions.assertThat(response)
                .extracting("recordId", "commentId", "content")
                .containsExactly(recordEntity.getId(), commentEntity.getId(), changedContent);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정하려고 하면 예외가 발생한다")
    void updateCommentWithNotExistingCommentIdTest() throws Exception {
        //given
        long userId = 1L;
        long notExistingCommentId = 0L;

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.updateComment(userId, notExistingCommentId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 수정하려고 하면 예외가 발생한다")
    void updateCommentWithNotCommenterTest() throws Exception {
        //given
        UserEntity commenter = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(commenter));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0)));

        CommentEntity commentEntity = commentRepository.save(createComment(commenter, recordEntity, null));

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("change content")
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.updateComment(other.getId(), commentEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("원댓글 작성자가 원댓글을 삭제하면 하위 댓글들도 함께 삭제된다")
    void deleteParentCommentTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        UserEntity commenter = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(author));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0)));

        CommentEntity parentCommentEntity = commentRepository.save(createComment(commenter, recordEntity, null));

        CommentEntity childCommentEntity1 = createComment(author, recordEntity, parentCommentEntity);
        CommentEntity childCommentEntity2 = createComment(author, recordEntity, parentCommentEntity);
        CommentEntity childCommentEntity3 = createComment(author, recordEntity, parentCommentEntity);

        commentRepository.saveAll(List.of(childCommentEntity1, childCommentEntity2, childCommentEntity3));

        //when
        commentService.deleteComment(commenter.getId(), parentCommentEntity.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 삭제하려고 하면 예외가 발생한다")
    void deleteCommentWithNotCommenterTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        UserEntity otherEntity = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0)));

        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(otherEntity.getId(), commentEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 삭제하려고 하면 예외가 발생한다")
    void deleteCommentWithNotExistingCommentTest() throws Exception {
        //given
        long notExistingCommentId = 0L;
        long userId = 1L;

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(userId, notExistingCommentId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.COMMENT_NOT_FOUND);
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("feed name")
                .startAt(LocalDateTime.of(2022, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, LocalDateTime date) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(date)
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
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