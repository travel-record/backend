package world.trecord.web.controller.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.MockMvcTestSupport;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.security.jwt.JwtTokenHandler;
import world.trecord.web.service.record.RecordService;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.*;

@MockMvcTestSupport
class RecordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RecordService recordService;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    JwtTokenHandler jwtTokenHandler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 성공")
    void getRecordInfoByWriterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.isUpdatable").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 성공 (인증되지 않은 사용자)")
    void getRecordInfoByWhoNotAuthenticatedTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test@email.com").build());
        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.isUpdatable").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 실패 (인증 토큰 검증 실패)")
    void getRecordInfoWithInvalidTokenTest() throws Exception {
        // when // then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", 0L)
                                .header("Authorization", "dummy")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getErrorMsg()));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 실패 (존재하지 않는 기록 아이디로 조회)")
    void getRecordInfoByNotExistingRecordIdTest() throws Exception {
        // when // then
        long notExistingRecordId = 0L;
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", notExistingRecordId)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(NOT_EXISTING_RECORD.getErrorCode()))
                .andExpect(jsonPath("$.message").value(NOT_EXISTING_RECORD.getErrorMsg()));
    }

    @Test
    @DisplayName("POST /api/v1/records - 실패 (올바르지 않은 요청 파라미터)")
    void createRecordWithInvalidParameterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        RecordCreateRequest request = RecordCreateRequest.builder()
                .build();

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_ARGUMENT.getErrorMsg()));
    }

    @Test
    @DisplayName("POST /api/v1/records - 성공")
    void createRecordWithValidParameterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        String title = "title";
        String place = "jeju";
        String feeling = "feeling";
        String weather = "weather";
        String satisfaction = "best";
        String content = "content";
        String companion = "companion";
        String imageUrl = "https://www.image.com";
        LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 1, 0, 0);

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title(title)
                .date(localDateTime)
                .place(place)
                .feeling(feeling)
                .weather(weather)
                .transportation(satisfaction)
                .content(content)
                .companion(companion)
                .imageUrl(imageUrl)
                .build();

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.feedId").value(feedEntity.getId()))
                .andExpect(jsonPath("$.data.recordId").exists())
                .andExpect(jsonPath("$.data.title").value(title))
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.date").value(localDateTime.toLocalDate().toString()))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl));
    }

    @Test
    @DisplayName("POST /api/v1/records - 실패 (피드 관리자가 아닌 사용자가 요청)")
    void createRecordTestWhenUserIsNotManager() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity viewer = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        String title = "title";
        String place = "jeju";
        String feeling = "feeling";
        String weather = "weather";
        String satisfaction = "best";
        String content = "content";
        String companion = "companion";
        String imageUrl = "https://www.image.com";
        LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 1, 0, 0);

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title(title)
                .date(localDateTime)
                .place(place)
                .feeling(feeling)
                .weather(weather)
                .transportation(satisfaction)
                .content(content)
                .companion(companion)
                .imageUrl(imageUrl)
                .build();

        String token = jwtTokenHandler.generateToken(viewer.getId(), secretKey, expiredTimeMs);

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.getErrorCode()));
    }

    @Test
    @DisplayName("PUT /api/v1/records/{recordId} - 성공")
    void updateRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String changedTitle = "change title";
        LocalDateTime changedDate = LocalDateTime.of(2021, 10, 2, 0, 0);
        String changedPlace = "changed place";
        String changedContent = "changed content";
        String changedFeeling = "changed feeling";
        String changedWeather = "changed weather";
        String changedCompanion = "changed changedCompanion";
        String changedSatisfaction = "changed satisfaction";
        String changedImageUrl = "changed image url";

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .title(changedTitle)
                .date(changedDate)
                .place(changedPlace)
                .content(changedContent)
                .feeling(changedFeeling)
                .weather(changedWeather)
                .companion(changedCompanion)
                .transportation(changedSatisfaction)
                .imageUrl(changedImageUrl)
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", recordEntity.getId())
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(changedTitle))
                .andExpect(jsonPath("$.data.content").value(changedContent))
                .andExpect(jsonPath("$.data.date").value(changedDate.toLocalDate().toString()));
    }

    @Test
    @DisplayName("PUT /api/v1/records/{recordId} - 실패 (피드 관리자가 아닌 사용자가 요청)")
    void updateRecordByNotManagerTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder().email("test@email.com").build());

        UserEntity other = userRepository.save(UserEntity.builder().email("test1@email.com").build());

        String token = jwtTokenHandler.generateToken(other.getId(), secretKey, expiredTimeMs);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .title("change title")
                .date(LocalDateTime.of(2021, 10, 2, 0, 0))
                .place("changed place")
                .content("changed content")
                .feeling("changed feeling")
                .weather("changed weather")
                .companion("changed changedCompanion")
                .transportation("changed satisfaction")
                .imageUrl("changed image url")
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", recordEntity.getId())
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.getErrorCode()));
    }

    @Test
    @DisplayName("PUT /api/v1/records - 실패 (올바르지 않은 요청 파라미터)")
    void updateRecordWithInvalidDataTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        RecordUpdateRequest request = RecordUpdateRequest.builder().build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", 0L)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/{recordId} - 실패 (올바르지 않은 경로 변수)")
    void deleteRecordWithInvalidDataTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        String invalidPathVariable = "invalid";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records/{recordId}", invalidPathVariable)
                                .header("Authorization", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/{recordId} - 성공")
    void deleteRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity commenter2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        String token = jwtTokenHandler.generateToken(writer.getId(), secretKey, expiredTimeMs);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records/{recordId}", recordEntity.getId())
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordId").value(recordEntity.getId()));

        Assertions.assertThat(recordRepository.findById(recordEntity.getId())).isEmpty();
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/records/{recordId}/like - 성공 (false 리턴)")
    void toggleLikeTestWhenUserLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtTokenHandler.generateToken(userEntity.getId(), secretKey, expiredTimeMs);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        userRecordLikeRepository.save(createUserRecordLikeEntity(userEntity, recordEntity));

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/{recordId}/like", recordEntity.getId())
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.liked").value(false));

    }

    @Test
    @DisplayName("POST /api/v1/records/{recordId}/like - 성공 (true 리턴)")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtTokenHandler.generateToken(userEntity.getId(), secretKey, expiredTimeMs);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/{recordId}/like", recordEntity.getId())
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId}/comments - 성공")
    void getRecordCommentsTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity commenter2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity2, commentEntity1));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}/comments", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.size()").value(2))
                .andExpect(jsonPath("$.data.comments[0].commentId").value(commentEntity2.getId()))
                .andExpect(jsonPath("$.data.comments[1].commentId").value(commentEntity1.getId()));
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

    private UserRecordLikeEntity createUserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }

}