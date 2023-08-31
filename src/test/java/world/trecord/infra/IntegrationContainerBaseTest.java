package world.trecord.infra;

import org.testcontainers.containers.MariaDBContainer;

@IntegrationTestSupport
public abstract class IntegrationContainerBaseTest {

    static final MariaDBContainer MARIA_DB_CONTAINER;

    static {
        MARIA_DB_CONTAINER = new MariaDBContainer("mariadb:latest");
        MARIA_DB_CONTAINER.start();
    }
}
