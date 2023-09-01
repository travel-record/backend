package world.trecord.web.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.web.controller.auth.request.GoogleLoginRequest;
import world.trecord.web.controller.auth.request.RefreshTokenRequest;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.security.JwtTokenHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

@MockMvcTestSupport
public class AuthControllerTest extends ContainerBaseTest {

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
    @DisplayName("POST /api/v1/auth/google-login - 실패 (올바르지 않은 파라미터)")
    void googleLoginWithEmptyAccessTokenTest() throws Exception {
        //given
        GoogleLoginRequest request = GoogleLoginRequest.builder().build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/google-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_ARGUMENT.getErrorMsg()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/token - 성공")
    void refreshTokenWithValidTokenTest() throws Exception {
        //given
        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .nickname("nickname")
                .build());

        String refreshToken = jwtTokenHandler.generateToken(savedUser.getId(), jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/token - 실패 (유효하지 않은 토큰)")
    void refreshTokenWithInvalidTokenTest() throws Exception {
        //given
        String refreshToken = "invalid token";

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/token - 실패 (파라미터 보내지 않음)")
    void refreshTokenWithEmptyTokenTest() throws Exception {
        //given
        RefreshTokenRequest request = RefreshTokenRequest.builder().build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_ARGUMENT.getErrorMsg()));
    }
}
