package world.trecord.infra;

import org.testcontainers.containers.MariaDBContainer;

import java.util.Arrays;

@IntegrationTestSupport
public abstract class AbstractContainerBaseTest {

    static final MariaDBContainer MARIA_DB_CONTAINER;

    static {
        MARIA_DB_CONTAINER = new MariaDBContainer("mariadb:latest")
                .withDatabaseName("trecord")
                .withUsername("test")
                .withPassword("1234");

        MARIA_DB_CONTAINER.setPortBindings(Arrays.asList("3307:3306"));
        MARIA_DB_CONTAINER.start();
    }
}
