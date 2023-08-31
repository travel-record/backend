package world.trecord.web.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.MockMvcContainerBaseTest;
import world.trecord.web.controller.auth.request.GoogleLoginRequest;
import world.trecord.web.controller.auth.request.RefreshTokenRequest;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

public class AuthControllerTest extends MockMvcContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenHandler jwtTokenHandler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProperties jwtProperties;

    @Test
    @DisplayName("POST /api/v1/auth/google-login - 실패 (파라미터 보내지 않음)")
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
    @DisplayName("POST /api/v1/auth/token - 성공")
    void refreshTokenWithValidTokenTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .nickname("nickname")
                .build();
        userRepository.save(userEntity);

        String refreshToken = jwtTokenHandler.generateToken(userEntity.getId(), jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());

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
    @DisplayName("POST /api/v1/auth/token - 실패 (유효하지 않은 토큰)")
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
    @DisplayName("POST /api/v1/auth/token - 실패 (파라미터 보내지 않음)")
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
