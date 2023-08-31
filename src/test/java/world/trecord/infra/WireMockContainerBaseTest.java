package world.trecord.infra;

import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.testcontainers.containers.MariaDBContainer;

@AutoConfigureWireMock(port = 8089)
@IntegrationTestSupport
public abstract class WireMockContainerBaseTest {

    static final MariaDBContainer MARIA_DB_CONTAINER;

    static {
        MARIA_DB_CONTAINER = new MariaDBContainer("mariadb:latest");
        MARIA_DB_CONTAINER.start();
    }
}
