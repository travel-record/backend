package world.trecord.controller.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.infra.test.AbstractMockMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PingControllerTest extends AbstractMockMvcTest {

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
}