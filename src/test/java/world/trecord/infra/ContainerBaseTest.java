package world.trecord.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MariaDBContainer;

@Slf4j
public class ContainerBaseTest implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static final MariaDBContainer MARIA_DB_CONTAINER;

    static {
        MARIA_DB_CONTAINER = new MariaDBContainer("mariadb:latest");
        MARIA_DB_CONTAINER.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("MARIA_DB_CONTAINER URL ==> {}", MARIA_DB_CONTAINER.getJdbcUrl());
        log.info("MARIA_DB_CONTAINER Username ==> {}", MARIA_DB_CONTAINER.getUsername());
        log.info("MARIA_DB_CONTAINER Password ==> {}", MARIA_DB_CONTAINER.getPassword());

        TestPropertyValues.of(
                "spring.datasource.url=" + MARIA_DB_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + MARIA_DB_CONTAINER.getUsername(),
                "spring.datasource.password=" + MARIA_DB_CONTAINER.getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}
