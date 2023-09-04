package world.trecord.web.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.properties.JwtProperties;
import world.trecord.service.comment.request.CommentCreateRequest;
import world.trecord.service.comment.request.CommentUpdateRequest;
import world.trecord.web.security.JwtTokenHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.COMMENT_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.INVALID_ARGUMENT;

@Transactional
@MockMvcTestSupport
class CommentControllerTest extends ContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenHandler jwtTokenHandler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProperties jwtProperties;

    @Test
    @DisplayName("POST /api/v1/comments - 성공")
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

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/comments - 성공 (대댓글 생성)")
    void createChildCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity parentCommentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        String content = "content";

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .parentId(parentCommentEntity.getId())
                .content(content)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/comments - 실패 (원댓글 존재하지 않음)")
    void createChildCommentWhenOriginCommentNotExistingTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

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
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/comments - 실패 (올바르지 못한 댓글)")
    void createCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        String invalidContent = "";
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(invalidContent)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("PUT /api/v1/comments/{commentId} - 성공")
    void updateCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        String changeContent = "change content";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(changeContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments/{commentId}", commentEntity.getId())
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value(changeContent));
    }

    @Test
    @DisplayName("PUT /api/v1/comments/{commentId} - 실패 (올바르지 파라미터)")
    void updateCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        String invalidContent = "";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(invalidContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments/{commentId}", commentEntity.getId())
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId} - 성공")
    void deleteCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity, null));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments/{commentId}", commentEntity.getId())
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId} - 실패 (올바르지 않은 경로 변수)")
    void deleteCommentWithCommentIdNullTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        String pathVariable = "Invalid path variable";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments/{commentId}", pathVariable)
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("GET /api/v1/comments/{commentId}/replies - 성공")
    void getRepliesTest() throws Exception {
        //given
        UserEntity author = createUser("test@email.com");
        UserEntity commenter1 = createUser("test1@email.com");
        UserEntity commenter2 = createUser("test2@email.com");
        UserEntity commenter3 = createUser("test3@email.com");

        userRepository.saveAll(List.of(author, commenter1, commenter2, commenter3));

        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));
        CommentEntity parentComment = commentRepository.save(createComment(commenter1, recordEntity, null));

        CommentEntity comment1 = createComment(commenter2, recordEntity, parentComment);
        CommentEntity comment2 = createComment(commenter3, recordEntity, parentComment);
        CommentEntity comment3 = createComment(commenter2, recordEntity, parentComment);
        CommentEntity comment4 = createComment(commenter3, recordEntity, parentComment);
        commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/comments/{commentId}/replies", parentComment.getId())
                                .header(AUTHORIZATION, createToken(author.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/comments/{commentId}/replies - 실패 (존재하지 않는 댓글)")
    void getRepliesWhenNotFoundCommentTest() throws Exception {
        //given
        long notExistingComment = 0L;
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/comments/{commentId}/replies", notExistingComment)
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2021, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2021, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 3, 2, 0, 0))
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