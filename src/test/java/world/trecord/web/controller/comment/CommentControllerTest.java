package world.trecord.web.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.MockMvcTestSupport;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.security.JwtProvider;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentDeleteRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;

@MockMvcTestSupport
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

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

    @Test
    @DisplayName("사용자가 기록에 댓글을 작성하면 댓글 상세 정보를 반환한다")
    void createCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String token = jwtProvider.createTokenWith(userEntity.getId());

        String content = "content";
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(content)
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .header("Authorization", token)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("content"));
    }

    @Test
    @DisplayName("올바르지 못한 댓글로 기록에 댓글을 작성하려고 하면 602 에러 응답 코드를 반환한다")
    void createCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String token = jwtProvider.createTokenWith(userEntity.getId());

        String invalidContent = "";
        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content(invalidContent)
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/comments")
                                .header("Authorization", token)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 작성자가 댓글을 수정하면 수정된 댓글 내용을 반환한다")
    void updateCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        String token = jwtProvider.createTokenWith(userEntity.getId());

        String changeContent = "change content";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .commentId(commentEntity.getId())
                .content(changeContent)
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments")
                                .header("Authorization", token)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value(changeContent));
    }

    @Test
    @DisplayName("올바르지 못한 댓글 내용으로 수정을 요청하면 602 에러 응답 코드를 반환한다")
    void updateCommentWithInvalidDataTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        String token = jwtProvider.createTokenWith(userEntity.getId());

        String invalidContent = "";
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .commentId(commentEntity.getId())
                .content(invalidContent)
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/comments")
                                .header("Authorization", token)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 작성자가 댓글을 삭제하면 삭제된 댓글 아이디를 반환한다")
    void deleteCommentTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        String token = jwtProvider.createTokenWith(userEntity.getId());

        CommentDeleteRequest request = CommentDeleteRequest.builder()
                .commentId(commentEntity.getId())
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments")
                                .header("Authorization", token)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(commentEntity.getId()));
        Assertions.assertThat(commentRepository.findById(commentEntity.getId())).isEmpty();
    }

    @Test
    @DisplayName("댓글 아이디가 null이면 602 에러 응답 코드를 반환한다")
    void deleteCommentWithCommentIdNullTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        CommentEntity commentEntity = commentRepository.save(createCommentEntity(userEntity, recordEntity, "content"));

        String token = jwtProvider.createTokenWith(userEntity.getId());

        CommentDeleteRequest request = CommentDeleteRequest.builder()
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        delete("/api/v1/comments")
                                .header("Authorization", token)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
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