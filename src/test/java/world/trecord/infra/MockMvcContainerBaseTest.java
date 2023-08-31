package world.trecord.infra;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MariaDBContainer;

@MockMvcTestSupport
public abstract class MockMvcContainerBaseTest {

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
