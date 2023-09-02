package world.trecord.web.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.COMMENT_NOT_FOUND;
import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;

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
        UserEntity userEntity = userRepository.save(createUser());

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
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/comments - 성공 (대댓글 생성)")
    void createChildCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity parentCommentEntity = commentRepository.save(createComment(userEntity, recordEntity));

        String content = "content";

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .parentId(parentCommentEntity.getId())
                .content(content)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .header("Authorization", createToken(userEntity.getId()))
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/comments - 실패 (원댓글 존재하지 않음)")
    void createChildCommentWhenOriginCommentNotExistingTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

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
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COMMENT_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/comments - 실패 (올바르지 못한 댓글)")
    void createCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

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
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
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
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity));

        String changeContent = "change content";

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(changeContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments/{commentId}", commentEntity.getId())
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value(changeContent));
    }

    @Test
    @DisplayName("PUT /api/v1/comments/{commentId} - 실패 (올바르지 파라미터)")
    void updateCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity));

        String invalidContent = "";

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content(invalidContent)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments/{commentId}", commentEntity.getId())
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId} - 성공")
    void deleteCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentEntity commentEntity = commentRepository.save(createComment(userEntity, recordEntity));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments/{commentId}", commentEntity.getId())
                                .header("Authorization", createToken(userEntity.getId()))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/comments/{commentId} - 실패 (올바르지 않은 경로 변수)")
    void deleteCommentWithCommentIdNullTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        String pathVariable = "Invalid path variable";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments/{commentId}", pathVariable)
                                .header("Authorization", createToken(userEntity.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
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

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content("content")
                .build();
    }

}