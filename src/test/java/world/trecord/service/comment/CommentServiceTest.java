package world.trecord.service.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.comment.request.CommentCreateRequest;
import world.trecord.dto.comment.request.CommentUpdateRequest;
import world.trecord.dto.comment.response.CommentResponse;
import world.trecord.dto.comment.response.UserCommentResponse;
import world.trecord.exception.CustomException;
import world.trecord.infra.fixture.CommentEntityFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static world.trecord.exception.CustomExceptionError.*;

@Transactional
class CommentServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자가 기록에 댓글을 작성하면 댓글 상세 정보를 반환한다")
    void createCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        String content = "content";
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(content)
                .build();

        //when
        commentService.createComment(userEntity.getId(), request);

        //then
        Assertions.assertThat(commentRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("자신의 기록에 댓글을 작성하면 알림이 생성되지 않는다")
    void createNotificationWhenCommentOnSelfTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

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
    @DisplayName("다른 사람의 기록에 댓글을 작성하면 비동기로 알림이 생성된다")
    void createCommentNotificationWhenCommentOnOtherRecordTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity commenter = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content("content")
                .build();

        //when
        commentService.createComment(commenter.getId(), request);

        //then
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Mockito.verify(mockEventListener, Mockito.times(1)).handleNotificationEventListener(Mockito.any()));
    }


    @Test
    @DisplayName("자신의 기록에 댓글을 남기면 알림이 전송되지 않는다")
    void createCommentNotificationWhenCommentOnSelfRecordTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        //when
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content("content")
                .build();

        //when
        commentService.createComment(owner.getId(), request);

        //then
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Mockito.verify(mockEventListener, Mockito.times(1)).handleNotificationEventListener(Mockito.any()));
    }

    @Test
    @DisplayName("대댓글을 page로 조회한다")
    void getRepliesTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity commenter = userRepository.save(UserEntityFixture.of("test1@email.com"));
        UserEntity replier = userRepository.save(UserEntityFixture.of("test2@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity originalComment = commentRepository.save(CommentEntityFixture.of(commenter, recordEntity, null));

        List<CommentEntity> replyComments = new ArrayList<>();
        int commentCnt = 100;
        for (int commentNumber = 0; commentNumber < commentCnt; commentNumber++) {
            replyComments.add(CommentEntityFixture.of(replier, recordEntity, originalComment));
        }

        commentRepository.saveAll(replyComments);

        int pageNumber = 0;
        int pageSize = 20;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<CommentResponse> response = commentService.getReplies(author.getId(), originalComment.getId(), pageRequest);

        //then
        Assertions.assertThat(response.getSize()).isEqualTo(pageSize);
        Assertions.assertThat(response.getNumber()).isEqualTo(pageNumber);
        Assertions.assertThat(response.getTotalPages()).isEqualTo(commentCnt / pageSize);
        Assertions.assertThat(response.getTotalElements()).isEqualTo(commentCnt);
    }

    @Test
    @DisplayName("원댓글이 존재하지 않으면 예외가 발생한다")
    void getRepliesWhenCommentsNotExistingTest() throws Exception {
        //given
        PageRequest pageRequest = PageRequest.of(0, 10);
        long notExistingCommentId = -1L;

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.getReplies(null, notExistingCommentId, pageRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("대댓글을 작성하여 생성된 댓글 상세 정보를 반환한다")
    void createChildCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity parentCommentEntity = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity, null));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .parentId(parentCommentEntity.getId())
                .content("content")
                .build();

        //when
        commentService.createComment(userEntity.getId(), request);

        //then
        Assertions.assertThat(commentRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("사용자가 작성한 댓글을 페이지네이션으로 조회하여 반환한다")
    void getUserCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity1 = recordRepository.save(RecordEntityFixture.of(feedEntity));
        RecordEntity recordEntity2 = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity1 = CommentEntityFixture.of(userEntity, recordEntity1);
        CommentEntity commentEntity2 = CommentEntityFixture.of(userEntity, recordEntity2);
        CommentEntity commentEntity3 = CommentEntityFixture.of(userEntity, recordEntity2);
        CommentEntity commentEntity4 = CommentEntityFixture.of(userEntity, recordEntity1);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        int pageNumber = 0;
        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<UserCommentResponse> page = commentService.getUserComments(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("사용자가 등록한 댓글이 없으면 빈 배열을 반환한다")
    void getUserEmptyCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        int pageNumber = 0;
        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<UserCommentResponse> page = commentService.getUserComments(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
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
                .isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 기록에 댓글을 달려고 하면 예외가 발생한다")
    void createCommentWithNotExistingRecordIdTest() throws Exception {
        //given
        long notExistingRecordId = 0L;
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(notExistingRecordId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(userEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 댓글에 답글을 달려고 하면 예외가 발생한다")
    void createCommentWhenCommentNotExistingTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        long notExistingCommentId = 0L;

        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("content")
                .recordId(recordEntity.getId())
                .parentId(notExistingCommentId)
                .build();

        //when //then

        Assertions.assertThatThrownBy(() -> commentService.createComment(author.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자가 댓글을 수정하면 수정된 댓글 내용을 반환한다")
    void updateCommentTest() throws Exception {
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity savedComment = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity, null));

        String changedContent = "changed content";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(changedContent)
                .build();

        //when
        commentService.updateComment(userEntity.getId(), savedComment.getId(), request);

        //then
        Assertions.assertThat(commentRepository.findById(savedComment.getId()))
                .isPresent()
                .hasValueSatisfying(
                        commentEntity -> {
                            Assertions.assertThat(commentEntity.getContent()).isEqualTo(changedContent);
                        }
                );
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
                .isEqualTo(COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 수정하려고 하면 예외가 발생한다")
    void updateCommentWithNotCommenterTest() throws Exception {
        //given
        UserEntity commenter = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(commenter));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = commentRepository.save(CommentEntityFixture.of(commenter, recordEntity, null));

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("change content")
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.updateComment(other.getId(), commentEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("원댓글 작성자가 원댓글을 삭제하면 하위 댓글들도 함께 삭제된다")
    @Transactional
    void deleteParentCommentTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity commenter = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity parentCommentEntity = commentRepository.save(CommentEntityFixture.of(commenter, recordEntity, null));
        CommentEntity childCommentEntity1 = CommentEntityFixture.of(author, recordEntity, parentCommentEntity);
        CommentEntity childCommentEntity2 = CommentEntityFixture.of(author, recordEntity, parentCommentEntity);
        CommentEntity childCommentEntity3 = CommentEntityFixture.of(author, recordEntity, parentCommentEntity);

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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity otherEntity = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity, null));

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(otherEntity.getId(), commentEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
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
                .isEqualTo(COMMENT_NOT_FOUND);
    }
}