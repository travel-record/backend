package world.trecord.web.controller.users;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import world.trecord.web.security.jwt.JwtGenerator;
import world.trecord.web.service.users.request.UserUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.*;

@MockMvcTestSupport
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtGenerator jwtGenerator;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Test
    @DisplayName("사용자 아이디로 사용자 정보를 반환한다")
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
        String token = jwtGenerator.generateToken(saveUser.getId());

        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디를 암호화한 토큰으로 사용자 정보를 조회하면 601 에러 응답 코드를 반환한다")
    void getUserInfoWithNotExistingTokenTest() throws Exception {
        //given
        String token = jwtGenerator.generateToken(-1L);

        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                                .header("Authorization", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getErrorMsg()));
    }

    @Test
    @DisplayName("사용자 정보를 등록하고, 등록한 정보를 반환한다")
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

        String token = jwtGenerator.generateToken(saveUser.getId());

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/users")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("이미 등록된 닉네임으로 사용자 정보를 등록하면 700 에러 응답 코드를 반환한다")
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

        String token = jwtGenerator.generateToken(requestUserEntity.getId());

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(duplicatedNickname)
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/users")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(EXISTING_NICKNAME.getErrorCode()))
                .andExpect(jsonPath("$.message").value(EXISTING_NICKNAME.getErrorMsg()));
    }

    @Test
    @DisplayName("사용자 아이디로 다른 사용자 정보를 반환한다")
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
    @DisplayName("존재하지 않는 사용자 아이디로 사용자 프로필을 조회하면 701 에러 응답 코드를 반환한다")
    void getUserInfoByUserIdWithTest() throws Exception {
        //given

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/{userId}", 0L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(NOT_EXISTING_USER.getErrorCode()))
                .andExpect(jsonPath("$.message").value(NOT_EXISTING_USER.getErrorMsg()));
    }

    @Test
    @DisplayName("사용자 아이디로 사용자 댓글 리스트를 반환한다")
    void getUserCommentsTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        String token = jwtGenerator.generateToken(userEntity.getId());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity1 = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        RecordEntity recordEntity2 = recordRepository.save(createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        String content4 = "content4";

        CommentEntity commentEntity1 = createCommentEntity(userEntity, recordEntity1, content1);
        CommentEntity commentEntity2 = createCommentEntity(userEntity, recordEntity2, content2);
        CommentEntity commentEntity3 = createCommentEntity(userEntity, recordEntity2, content3);
        CommentEntity commentEntity4 = createCommentEntity(userEntity, recordEntity1, content4);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments.size()").value(4))
                .andExpect(jsonPath("$.data.comments[0].commentId").value(commentEntity4.getId()));
    }

    @Test
    @DisplayName("올바르지 않은 인증 토큰으로 사용자 댓글 리스트를 조회하면 601 에러 응답 코드를 반환한다")
    void getUserCommentsWithNotExistingUserIdTest() throws Exception {
        //given
        String invalidToken = "-1";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                                .header("Authorization", invalidToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getErrorCode()));
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

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }
}