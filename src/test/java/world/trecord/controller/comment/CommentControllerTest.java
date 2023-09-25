package world.trecord.controller.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.comment.request.CommentCreateRequest;
import world.trecord.dto.comment.request.CommentUpdateRequest;
import world.trecord.infra.fixture.CommentEntityFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.support.WithTestUser;
import world.trecord.infra.test.AbstractMockMvcTest;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.COMMENT_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.INVALID_ARGUMENT;

@Transactional
class CommentControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("POST /api/v1/comments - 성공")
    @WithTestUser("commenter@email.com")
    void createCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("commenter@email.com").get();
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        String content = "content";

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(content)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/comments - 성공 (대댓글 생성)")
    @WithTestUser("commenter@email.com")
    void createChildCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity parentCommentEntity = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity));

        String content = "content";

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .parentId(parentCommentEntity.getId())
                .content(content)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .content(body(request))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/comments - 실패 (원댓글 존재하지 않음)")
    @WithTestUser("commenter@email.com")
    void createChildCommentWhenOriginCommentNotExistingTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        long notExistingCommentId = 0L;
        String content = "content";
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .parentId(notExistingCommentId)
                .content(content)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/comments - 실패 (유효하지 않은 파라미터)")
    @WithTestUser("commenter@email.com")
    void createCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        String invalidContent = "";
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(invalidContent)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/comments/{commentId} - 성공")
    @WithTestUser("commenter@email.com")
    void updateCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("commenter@email.com").get();
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity));

        String changeContent = "change content";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(changeContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments/{commentId}", commentEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/comments/{commentId} - 실패 (올바르지 않은 파라미터)")
    @WithTestUser("commenter@email.com")
    void updateCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("commenter@email.com").get();
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity));

        String invalidContent = "";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(invalidContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments/{commentId}", commentEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId} - 성공")
    @WithTestUser("commenter@email.com")
    void deleteCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("commenter@email.com").get();
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity commentEntity = commentRepository.save(CommentEntityFixture.of(userEntity, recordEntity, null));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments/{commentId}", commentEntity.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId} - 실패 (올바르지 않은 경로 변수)")
    @WithTestUser
    void deleteCommentWithCommentIdNullTest() throws Exception {
        //given
        String pathVariable = "Invalid path variable";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments/{commentId}", pathVariable)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/comments/{commentId}/replies - 성공")
    @WithTestUser("viewer@email.com")
    void getRepliesTest() throws Exception {
        //given
        UserEntity author = UserEntityFixture.of("test@email.com");
        UserEntity commenter1 = UserEntityFixture.of("test1@email.com");
        UserEntity commenter2 = UserEntityFixture.of("test2@email.com");
        UserEntity commenter3 = UserEntityFixture.of("test3@email.com");

        userRepository.saveAll(List.of(author, commenter1, commenter2, commenter3));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(author));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));
        CommentEntity parentComment = commentRepository.save(CommentEntityFixture.of(commenter1, recordEntity, null));

        CommentEntity comment1 = CommentEntityFixture.of(commenter2, recordEntity, parentComment);
        CommentEntity comment2 = CommentEntityFixture.of(commenter3, recordEntity, parentComment);
        CommentEntity comment3 = CommentEntityFixture.of(commenter2, recordEntity, parentComment);
        CommentEntity comment4 = CommentEntityFixture.of(commenter3, recordEntity, parentComment);
        commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/comments/{commentId}/replies", parentComment.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/comments/{commentId}/replies - 실패 (존재하지 않는 댓글)")
    @WithTestUser
    void getRepliesWhenNotFoundCommentTest() throws Exception {
        //given
        long notExistingComment = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/comments/{commentId}/replies", notExistingComment)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }
}