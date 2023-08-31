package world.trecord.infra;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.testcontainers.containers.MariaDBContainer;

@AutoConfigureWireMock(port = 8089)
@IntegrationTestSupport
public abstract class WireMockContainerBaseTest {

    static final MariaDBContainer MARIA_DB_CONTAINER;

    static {
        MARIA_DB_CONTAINER = new MariaDBContainer("mariadb:latest")
                .withDatabaseName("trecord")
                .withUsername("test")
                .withPassword("1234");
        MARIA_DB_CONTAINER.start();
    }

    @BeforeAll
    public static void setUp() {
        System.setProperty("TC_MARIADB_JDBC_URL", MARIA_DB_CONTAINER.getJdbcUrl());
    }
}
