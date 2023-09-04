package world.trecord.controller.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
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
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.properties.JwtProperties;
import world.trecord.service.users.request.UserUpdateRequest;
import world.trecord.config.security.JwtTokenHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
@MockMvcTestSupport
class UserControllerTest extends ContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTokenHandler jwtTokenHandler;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    JwtProperties jwtProperties;

    @Test
    @DisplayName("GET /api/v1/users - 성공")
    void getUserInfoTest() throws Exception {
        //given
        String email = "test@email.com";
        String nickname = "nickname";
        String imageUrl = "http://localhost/pictures";
        String introduction = "hello";

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        UserEntity saveUser = userRepository.save(userEntity);


        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                                .header("Authorization", createToken(saveUser.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("GET /api/v1/users - 실패 (존재하지 않는 사용자 아이디)")
    void getUserInfoWithNotExistingTokenTest() throws Exception {
        //given
        long notExistingUserId = -1L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                                .header("Authorization", createToken(notExistingUserId))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/users - 성공")
    void updateUserInfoTest() throws Exception {
        //given
        String nickname = "changed nickname";
        String imageUrl = "changed image url";
        String introduction = "change introduction";

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .nickname("before nickname")
                .imageUrl("before image url")
                .introduction("before introduction")
                .build();

        UserEntity saveUser = userRepository.save(userEntity);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/users")
                                .header("Authorization", createToken(saveUser.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("POST /api/v1/users - 실패 (이미 등록된 닉네임)")
    void updateUserInfoWithExistingNicknameTest() throws Exception {
        //given
        String duplicatedNickname = "duplicate nickname";

        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .nickname(duplicatedNickname)
                .build();

        userRepository.save(userEntity);

        UserEntity requestUserEntity = UserEntity.builder()
                .email("test1@email.com")
                .nickname("nickname")
                .build();

        userRepository.save(requestUserEntity);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(duplicatedNickname)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/users")
                                .header("Authorization", createToken(requestUserEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(NICKNAME_DUPLICATED.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - 성공")
    void getUserInfoByUserIdTest() throws Exception {
        //given
        String email = "test@email.com";
        String nickname = "nickname";
        String imageUrl = "http://localhost/pictures";
        String introduction = "hello";

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        UserEntity saveUser = userRepository.save(userEntity);

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/{userId}", saveUser.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - 실패 (존재하지 않는 사용자 아이디)")
    void getUserInfoByUserIdWithTest() throws Exception {
        //given
        long notExistingUserId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/{userId}", notExistingUserId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/comments - 성공")
    void getUserCommentsTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0)));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0)));

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        String content4 = "content4";

        CommentEntity commentEntity1 = createComment(userEntity, recordEntity1, content1);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity2, content2);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity2, content3);
        CommentEntity commentEntity4 = createComment(userEntity, recordEntity1, content4);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                                .header("Authorization", createToken(userEntity.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.size()").value(4))
                .andExpect(jsonPath("$.data.comments[0].commentId").value(commentEntity4.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/users/comments - 실패 (유효하지 않은 토큰)")
    void getUserCommentsWithNotExistingUserIdTest() throws Exception {
        //given
        String invalidToken = "-1";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                                .header("Authorization", invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/likes - 성공")
    void getUserRecordLikesTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser());

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecord(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity2 = createRecord(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity3 = createRecord(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity4 = createRecord(feedEntity, "record4", "place4", LocalDateTime.of(2022, 3, 2, 0, 0));

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(userEntity, recordEntity4);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/likes")
                                .header("Authorization", createToken(userEntity.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.size()").value(2))
                .andExpect(jsonPath("$.data.records[0].recordId").value(recordEntity4.getId()))
                .andExpect(jsonPath("$.data.records[0].title").value(recordEntity4.getTitle()))
                .andExpect(jsonPath("$.data.records[0].imageUrl").value(recordEntity4.getImageUrl()))
                .andExpect(jsonPath("$.data.records[0].authorId").value(userEntity.getId()))
                .andExpect(jsonPath("$.data.records[0].authorNickname").value(userEntity.getNickname()));
    }


    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email("test@email.com")
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

    private RecordEntity createRecord(FeedEntity feedEntity, String title, String place, LocalDateTime date) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(title)
                .place(place)
                .date(date)
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
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
}