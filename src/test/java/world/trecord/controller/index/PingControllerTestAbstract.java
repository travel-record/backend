package world.trecord.controller.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTestSupport
class PingControllerTestAbstract extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("GET / - 성공")
    void pingTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }


    @Test
    @DisplayName("GET / - 실패 (유효하지 않은 토큰)")
    void pingWithTokenTest() throws Exception {
        //given
        String invalidToken = "invalid";

        //when //then
        mockMvc.perform(
                        get("/")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized());
    }
}