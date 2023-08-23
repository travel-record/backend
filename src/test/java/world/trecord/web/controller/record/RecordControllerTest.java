package world.trecord.web.controller.record;

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
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.security.jwt.JwtGenerator;
import world.trecord.web.service.record.RecordService;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordDeleteRequest;
import world.trecord.web.service.record.request.RecordLikeRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

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
    JwtGenerator jwtGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Test
    @DisplayName("기록 작성자가 기록을 조회하면 기록 상세 정보와 댓글 리스트를 반환한다")
    void getRecordInfoByWriterTest() throws Exception {
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

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        String token = jwtGenerator.generateToken(writer.getId());

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))

                .andExpect(jsonPath("$.data.isUpdatable").value(true))
                .andExpect(jsonPath("$.data.comments[0].isUpdatable").value(false))
                .andExpect(jsonPath("$.data.comments[1].isUpdatable").value(false));
    }

    @Test
    @DisplayName("댓글 작성자가 기록을 조회하면 기록 상세 정보와 댓글 리스트를 반환한다")
    void getRecordInfoByCommenterTest() throws Exception {
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

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        String token = jwtGenerator.generateToken(commenter1.getId());

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.isUpdatable").value(false))
                .andExpect(jsonPath("$.data.comments[0].isUpdatable").value(true))
                .andExpect(jsonPath("$.data.comments[1].isUpdatable").value(false));
    }

    @Test
    @DisplayName("익명 사용자가 기록을 조회하면 기록 상세 정보와 댓글 리스트를 반환한다")
    void getRecordInfoByStrangerTest() throws Exception {
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

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.isUpdatable").value(false))
                .andExpect(jsonPath("$.data.comments[0].isUpdatable").value(false))
                .andExpect(jsonPath("$.data.comments[1].isUpdatable").value(false));
    }

    @Test
    @DisplayName("유효하지 않은 인증 코드로 기록 아이디를 조회하면 601 에러 응답 코드를 반환한다")
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
    @DisplayName("존재하지 않은 기록 아이디로 조회하면 703 에러 응답 코드를 반환한다")
    void getRecordInfoByNotExistingRecordIdTest() throws Exception {
        // when // then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", 0L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CustomExceptionError.NOT_EXISTING_RECORD.getErrorCode()))
                .andExpect(jsonPath("$.message").value(CustomExceptionError.NOT_EXISTING_RECORD.getErrorMsg()));
    }

    @Test
    @DisplayName("올바르지 않은 요청 데이터로 전송하면 602 에러 응답 코드를 반환한다")
    void createRecordWithInvalidParameterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        RecordCreateRequest request = RecordCreateRequest.builder()
                .build();

        String token = jwtGenerator.generateToken(writer.getId());

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CustomExceptionError.INVALID_ARGUMENT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(CustomExceptionError.INVALID_ARGUMENT.getErrorMsg()));
    }

    @Test
    @DisplayName("피드 작성자가 올바른 요청 데이터로 기록 생성 요청을 하면 생성된 기록 정보를 반환한다")
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

        String token = jwtGenerator.generateToken(writer.getId());

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
    @DisplayName("피드 작성자가 올바른 데이터로 기록 수정 요청을 하면 수정된 기록 정보를 반환한다")
    void updateRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtGenerator.generateToken(writer.getId());

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
                .feedId(feedEntity.getId())
                .recordId(recordEntity.getId())
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
                        put("/api/v1/records")
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
    @DisplayName("피드 작성자가 올바르지 않은 데이터로 기록 수정 요청을 하면 602 에러 응답 코드로 반환한다")
    void updateRecordWithInvalidDataTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtGenerator.generateToken(writer.getId());

        RecordUpdateRequest request = RecordUpdateRequest.builder().build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CustomExceptionError.INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("올바르지 않은 요청 파라미터로 기록을 삭제하려고 하면 602 에러 응답 코드로 반환한다")
    void deleteRecordWithInvalidDataTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtGenerator.generateToken(writer.getId());

        RecordDeleteRequest request = RecordDeleteRequest.builder().build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CustomExceptionError.INVALID_ARGUMENT.getErrorCode()));
    }

    @Test
    @DisplayName("올바른 요청 파라미터로 기록을 삭제하면 댓글들과 함께 삭제한다")
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

        String token = jwtGenerator.generateToken(writer.getId());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        RecordDeleteRequest request = RecordDeleteRequest.builder()
                .feedId(feedEntity.getId())
                .recordId(recordEntity.getId())
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordId").value(recordEntity.getId()));

        Assertions.assertThat(recordRepository.findById(recordEntity.getId())).isEmpty();
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록에 좋아요를 요청하면 liked=false 응답을 한다")
    void toggleLikeTestWhenUserLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtGenerator.generateToken(userEntity.getId());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        userRecordLikeRepository.save(createUserRecordLikeEntity(userEntity, recordEntity));

        RecordLikeRequest request = RecordLikeRequest.builder()
                .recordId(recordEntity.getId())
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/like")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.liked").value(false));

    }

    @Test
    @DisplayName("사용자가 좋아요 하지 않은 기록에 좋아요를 요청하면 liked=true 응답을 한다")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtGenerator.generateToken(userEntity.getId());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        RecordLikeRequest request = RecordLikeRequest.builder()
                .recordId(recordEntity.getId())
                .build();

        String body = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/like")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
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