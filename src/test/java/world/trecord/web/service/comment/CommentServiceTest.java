package world.trecord.web.service.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationRepository;
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
import world.trecord.web.service.comment.response.UserCommentsResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

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

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    @DisplayName("사용자가 기록에 댓글을 작성하면 댓글 상세 정보를 반환한다")
    void createCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

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
    @DisplayName("다른 사람의 기록에 댓글을 작성하면 알림이 생성된다")
    void createCommentNotificationWhenCommentOnOtherRecordTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        UserEntity commenter = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content("content")
                .build();

        //when
        commentService.createComment(commenter.getId(), request);

        //then
        Assertions.assertThat(notificationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("자신의 기록에 댓글을 작성하면 알림이 생성되지 않는다")
    void createCommentNotificationWhenCommentOnSelfTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content("content")
                .build();

        //when
        commentService.createComment(author.getId(), request);

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("대댓글을 작성하여 생성된 댓글 상세 정보를 반환한다")
    void createChildCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
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
    @DisplayName("사용자가 작성한 댓글을 등록 시간 내림차순으로 조회하여 반환한다")
    void getUserCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity));
        CommentEntity commentEntity1 = createComment(userEntity, recordEntity1, null);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity2, null);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity2, null);
        CommentEntity commentEntity4 = createComment(userEntity, recordEntity1, null);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when
        UserCommentsResponse response = commentService.getUserComments(userEntity.getId());

        //then
        Assertions.assertThat(response.getComments())
                .hasSize(4)
                .extracting("recordId", "commentId", "content")
                .containsExactly(
                        tuple(recordEntity1.getId(), commentEntity4.getId(), commentEntity4.getContent()),
                        tuple(recordEntity2.getId(), commentEntity3.getId(), commentEntity3.getContent()),
                        tuple(recordEntity2.getId(), commentEntity2.getId(), commentEntity2.getContent()),
                        tuple(recordEntity1.getId(), commentEntity1.getId(), commentEntity1.getContent())
                );
    }

    @Test
    @DisplayName("사용자가 등록한 댓글이 없으면 빈 배열을 반환한다")
    void getUserEmptyCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        UserCommentsResponse response = commentService.getUserComments(userEntity.getId());

        //then
        Assertions.assertThat(response.getComments()).isEmpty();
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
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
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
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
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
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
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
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
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

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 10, 10, 0, 0))
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