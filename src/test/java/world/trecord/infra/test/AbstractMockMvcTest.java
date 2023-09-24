package world.trecord.infra.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.security.JwtTokenHandler;

@AutoConfigureMockMvc
public abstract class AbstractMockMvcTest extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtProperties jwtProperties;

    @Autowired
    protected JwtTokenHandler jwtTokenHandler;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String body(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    protected String token(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }
}
