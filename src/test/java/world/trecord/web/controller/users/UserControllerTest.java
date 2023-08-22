package world.trecord.web.controller.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.MockMvcTestSupport;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.security.JwtProvider;
import world.trecord.web.service.users.request.UserUpdateRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

@MockMvcTestSupport
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

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
        String token = jwtProvider.createTokenWith(saveUser.getId());

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
        String token = jwtProvider.createTokenWith(-1L);

        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                                .header("Authorization", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(CustomExceptionError.INVALID_TOKEN.getErrorMsg()));
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

        String token = jwtProvider.createTokenWith(saveUser.getId());

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

        String token = jwtProvider.createTokenWith(requestUserEntity.getId());

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
                .andExpect(jsonPath("$.code").value(CustomExceptionError.EXISTING_NICKNAME.getErrorCode()))
                .andExpect(jsonPath("$.message").value(CustomExceptionError.EXISTING_NICKNAME.getErrorMsg()));
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
    @DisplayName("존재하지 않는 사용자 아이디로 조회하면 701 에러 응답 코드를 반환한다")
    void getUserInfoByUserIdWithTest() throws Exception {
        //given

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/{userId}", 0L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CustomExceptionError.NOT_EXISTING_USER.getErrorCode()))
                .andExpect(jsonPath("$.message").value(CustomExceptionError.NOT_EXISTING_USER.getErrorMsg()));
    }

}