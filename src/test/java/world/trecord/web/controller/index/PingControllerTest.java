package world.trecord.web.controller.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.MockMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class PingControllerTest extends ContainerBaseTest {

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
}