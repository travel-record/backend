package world.trecord.infra;

import org.testcontainers.containers.MariaDBContainer;

@MockMvcTestSupport
public abstract class MockMvcContainerBaseTest {

    static final MariaDBContainer MARIA_DB_CONTAINER;

    static {
        MARIA_DB_CONTAINER = new MariaDBContainer("mariadb:latest");
        MARIA_DB_CONTAINER.start();
    }
}
