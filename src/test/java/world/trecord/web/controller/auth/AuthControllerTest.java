package world.trecord.web.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.MockMvcTestSupport;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.controller.auth.request.GoogleLoginRequest;
import world.trecord.web.controller.auth.request.RefreshTokenRequest;
import world.trecord.web.security.JwtProvider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.*;

@MockMvcTestSupport
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("유효하지 않는 구글 인가 코드는 600 에러 코드로 반환한다")
    void googleLoginWithInvalidAccessTokenTest() throws Exception {
        //given
        GoogleLoginRequest request = GoogleLoginRequest.builder()
                .authorizationCode("dummy")
                .redirectionUri("dummy")
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/google-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_GOOGLE_AUTHORIZATION_CODE.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_GOOGLE_AUTHORIZATION_CODE.getErrorMsg()));
    }

    @Test
    @DisplayName("인가 코드를 전송하지 않으면 602 에러 코드로 반환한다")
    void googleLoginWithEmptyAccessTokenTest() throws Exception {
        //given
        GoogleLoginRequest request = GoogleLoginRequest.builder()
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/google-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_ARGUMENT.getErrorMsg()));
    }

    @Test
    @DisplayName("유효한 토큰으로 토큰을 재발급한다")
    void refreshTokenWithValidTokenTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .nickname("nickname")
                .build();
        userRepository.save(userEntity);

        String refreshToken = jwtProvider.createRefreshTokenWith(userEntity.getId());

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 재발급을 받으려고 하면 601 에러 응답 코드를 반환한다")
    void refreshTokenWithInvalidTokenTest() throws Exception {
        //given
        String refreshToken = "dummy";
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getErrorMsg()));
    }

    @Test
    @DisplayName("리프레시 토큰을 전송하지 않으면 602 에러 응답 코드를 반환한다")
    void refreshTokenWithEmptyTokenTest() throws Exception {
        //given
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_ARGUMENT.getErrorMsg()));
    }
}
