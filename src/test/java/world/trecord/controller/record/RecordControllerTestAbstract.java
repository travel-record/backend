package world.trecord.controller.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.security.JwtTokenHandler;
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
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.service.record.RecordService;
import world.trecord.service.record.request.RecordCreateRequest;
import world.trecord.service.record.request.RecordSequenceSwapRequest;
import world.trecord.service.record.request.RecordUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
@MockMvcTestSupport
class RecordControllerTestAbstract extends AbstractContainerBaseTest {

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

    @Autowired
    JwtProperties jwtProperties;

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 성공")
    void getRecordInfoByWriterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                                .header(AUTHORIZATION, createToken(writer.getId()))
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
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

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
        //given
        String invalidToken = "invalid token";

        //when // then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", 0L)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.message()));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 실패 (존재하지 않는 기록 아이디로 조회)")
    void getRecordInfoByNotExistingRecordIdTest() throws Exception {
        //given
        long notExistingRecordId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", notExistingRecordId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(RECORD_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records - 실패 (올바르지 않은 요청 파라미터)")
    void createRecordWithInvalidParameterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        RecordCreateRequest request = RecordCreateRequest.builder()
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header(AUTHORIZATION, createToken(writer.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records - 성공")
    void createRecordWithValidParameterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

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

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header(AUTHORIZATION, createToken(writer.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(recordRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/records - 실패 (피드 관리자가 아닌 사용자가 요청)")
    void createRecordTestWhenUserIsNotManager() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test1@email.com"));
        UserEntity viewer = userRepository.save(createUser("test2@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

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

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header(AUTHORIZATION, createToken(viewer.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/records/{recordId} - 성공")
    void updateRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));

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

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", recordEntity.getId())
                                .header(AUTHORIZATION, createToken(writer.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
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
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        UserEntity other = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));

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

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", recordEntity.getId())
                                .header(AUTHORIZATION, createToken(other.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records/swap - 성공")
    void swapRecordSequenceTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        int seq1 = 1;
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), seq1));

        int seq2 = 2;
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), seq2));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/swap")
                                .header(AUTHORIZATION, createToken(writer.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(recordRepository.findById(recordEntity1.getId()))
                .isPresent()
                .hasValueSatisfying(recordEntity -> {
                    Assertions.assertThat(recordEntity.getSequence()).isEqualTo(seq2);
                });

        Assertions.assertThat(recordRepository.findById(recordEntity2.getId()))
                .isPresent()
                .hasValueSatisfying(recordEntity -> {
                    Assertions.assertThat(recordEntity.getSequence()).isEqualTo(seq1);
                });
    }

    @Test
    @DisplayName("POST /api/v1/records/swap - 실패 (같은 피드 아이디가 아닌 경우)")
    void swapRecordSequenceWhenRecordNotSameFeedTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity1 = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        FeedEntity feedEntity2 = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity1, LocalDateTime.of(2021, 10, 1, 0, 0), 0));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity2, LocalDateTime.of(2021, 10, 1, 0, 0), 1));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/swap")
                                .header(AUTHORIZATION, createToken(writer.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records/swap - 실패 (피드 관리자가 아닌 경우)")
    void swapRecordSequenceByNotFeedManagerTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 1));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/swap")
                                .header(AUTHORIZATION, createToken(other.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records/swap - 실패 (존재하지 않는 기록 아이디로 요청)")
    void swapRecordSequenceWhenRecordNotExistingTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        long notExistingRecordId = 0L;

        String token = createToken(writer.getId());

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(notExistingRecordId)
                .targetRecordId(notExistingRecordId)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/swap")
                                .header(AUTHORIZATION, token)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(RECORD_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/records - 실패 (올바르지 않은 요청 파라미터)")
    void updateRecordWithInvalidDataTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        RecordUpdateRequest request = RecordUpdateRequest.builder().build();

        String token = createToken(writer.getId());

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", 0L)
                                .header(AUTHORIZATION, token)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/{recordId} - 실패 (인증되지 않는 사용자)")
    void deleteRecordWithInvalidDataTest() throws Exception {
        //given
        String invalidPathVariable = "invalid";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records/{recordId}", invalidPathVariable)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/{recordId} - 성공")
    void deleteRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity commenter1 = userRepository.save(createUser("test1@email.com"));
        UserEntity commenter2 = userRepository.save(createUser("test2@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        String token = createToken(writer.getId());

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records/{recordId}", recordEntity.getId())
                                .header(AUTHORIZATION, token)
                )
                .andExpect(status().isOk());

        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/records/{recordId}/like - 성공 (false 리턴)")
    void toggleLikeTestWhenUserLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        userRecordLikeRepository.save(createRecordLike(userEntity, recordEntity));

        String token = createToken(userEntity.getId());

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/{recordId}/like", recordEntity.getId())
                                .header(AUTHORIZATION, token)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.liked").value(false));

    }

    @Test
    @DisplayName("POST /api/v1/records/{recordId}/like - 성공 (true 리턴)")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        String token = createToken(userEntity.getId());

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/{recordId}/like", recordEntity.getId())
                                .header(AUTHORIZATION, token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId}/comments - 성공")
    void getRecordCommentsTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity commenter1 = userRepository.save(createUser("test1@email.com"));
        UserEntity commenter2 = userRepository.save(createUser("test2@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity2, commentEntity1));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}/comments", recordEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.size()").value(2));
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, LocalDateTime date, int sequence) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(date)
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

    private UserRecordLikeEntity createRecordLike(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }


    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }
}