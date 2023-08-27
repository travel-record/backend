package world.trecord.web.service.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentCreateResponse;
import world.trecord.web.service.comment.response.CommentDeleteResponse;
import world.trecord.web.service.comment.response.CommentUpdateResponse;

import java.time.LocalDateTime;

@IntegrationTestSupport
class CommentServiceTest {

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
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content("content")
                .build();

        //when
        CommentCreateResponse response = commentService.createComment(userEntity.getId(), request);

        //then
        CommentEntity commentEntity = commentRepository.findById(response.getCommentId()).get();
        Assertions.assertThat(response)
                .extracting("recordId", "commentId", "content")
                .containsExactly(recordEntity.getId(), commentEntity.getId(), commentEntity.getContent());
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 기록에 댓글을 달려고 하면 예외가 발생한다")
    void createCommentWithNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        CommentCreateRequest request = CommentCreateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(notExistingUserId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("존재하지 않는 기록에 댓글을 달려고 하면 예외가 발생한다")
    void createCommentWithNotExistingRecordIdTest() throws Exception {
        //given

        Long notExistingRecordId = 0L;

        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(notExistingRecordId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.createComment(userEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_RECORD);
    }

    @Test
    @DisplayName("댓글 작성자가 댓글을 수정하면 수정된 댓글 내용을 반환한다")
    void updateCommentTest() throws Exception {
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        String changedContent = "changed content";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .commentId(commentEntity.getId())
                .content(changedContent)
                .build();

        //when
        CommentUpdateResponse response = commentService.updateComment(userEntity.getId(), request);

        //then
        Assertions.assertThat(response)
                .extracting("recordId", "commentId", "content")
                .containsExactly(recordEntity.getId(), commentEntity.getId(), changedContent);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 기록에 댓글을 수정하려고 하면 예외가 발생한다")
    void updateCommentWithNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.updateComment(notExistingUserId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정하려고 하면 예외가 발생한다")
    void updateCommentWithNotExistingCommentIdTest() throws Exception {
        //given
        Long notExistingCommentId = 0L;

        UserEntity author = userRepository.save(UserEntity.builder().email("test@email.com").build());
        UserEntity other = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .commentId(notExistingCommentId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.updateComment(other.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_COMMENT);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 수정하려고 하면 예외가 발생한다")
    void updateCommentWithNotCommenterTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        UserEntity otherEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .commentId(commentEntity.getId())
                .content("change content")
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.updateComment(otherEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("댓글 작성자가 댓글을 삭제하면 삭제된 댓글 아이디를 반환한다")
    void deleteCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        //when
        CommentDeleteResponse response = commentService.deleteComment(userEntity.getId(), commentEntity.getId());

        //then
        Assertions.assertThat(response.getCommentId()).isEqualTo(commentEntity.getId());
        Assertions.assertThat(commentRepository.findById(commentEntity.getId())).isEmpty();
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 댓글을 삭제하려고 하면 예외가 발생한다")
    void deleteCommentWithNotCommenterTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        UserEntity otherEntity = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

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
        Long notExistingCommentId = 0L;
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(userEntity.getId(), notExistingCommentId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_COMMENT);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 댓글을 삭제하려고 하면 예외가 발생한다")
    void deleteCommentWithNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;
        Long notExistingCommentId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> commentService.deleteComment(notExistingUserId, notExistingCommentId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }


    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
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

    private CommentEntity createCommentEntity(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

}