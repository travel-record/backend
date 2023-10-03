package world.trecord.controller.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;
import world.trecord.controller.auth.request.GoogleLoginRequest;
import world.trecord.dto.auth.response.LoginResponse;
import world.trecord.infra.test.AbstractRestDocsTest;
import world.trecord.service.auth.AuthService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerDocsTest extends AbstractRestDocsTest {

    private final AuthService authService = Mockito.mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @Test
    @DisplayName("구글 로그인 API")
    void googleLogin_restDocs() throws Exception {
        //given
        String nickname = "nickname";
        Long userId = 1L;
        String token = "token";
        String refreshToken = "refresh token";
        String authorizationCode = "google authorization code";
        String redirectionUri = "http://localhost:3000";

        GoogleLoginRequest request = GoogleLoginRequest.builder()
                .authorizationCode(authorizationCode)
                .redirectionUri(redirectionUri)
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .token(token)
                .refreshToken(refreshToken)
                .build();

        given(authService.googleLogin(anyString(), anyString()))
                .willReturn(loginResponse);

        //when //then
        mockMvc.perform(post("/api/v1/auth/google-login")
                        .contentType(APPLICATION_JSON)
                        .content(body(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("google-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("구글 로그인 인가 코드"),
                                fieldWithPath("redirectionUri").type(JsonFieldType.STRING).description("리디렉션 URI")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메시지"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.user.userId").type(JsonFieldType.NUMBER).description("사용자 고유 아이디"),
                                fieldWithPath("data.user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임").optional(),
                                fieldWithPath("data.token.token").type(JsonFieldType.STRING).description("인증 토큰"),
                                fieldWithPath("data.token.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
                        ))
                );
    }

}
