package world.trecord.infra.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class AbstractMockMvcTest extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
}
