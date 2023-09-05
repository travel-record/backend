package world.trecord.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

@Slf4j
public class ContainerBaseTest implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static final DockerComposeContainer DOCKER_COMPOSE_CONTAINER;
    static final String MARIADB_SERVICE_NAME = "mariadb_1";
    static final String REDIS_SERVICE_NAME = "redis_1";
    static final int MARIADB_SERVICE_PORT = 3306;
    static final int REDIS_SERVICE_PORT = 6379;

    static {
        DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer(new File("docker-compose-test.yml"))
                .withExposedService(MARIADB_SERVICE_NAME, MARIADB_SERVICE_PORT)
                .withExposedService(REDIS_SERVICE_NAME, REDIS_SERVICE_PORT);

        DOCKER_COMPOSE_CONTAINER.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // MariaDB
        String jdbcUrl = String.format("jdbc:mariadb://%s:%s/test",
                DOCKER_COMPOSE_CONTAINER.getServiceHost(MARIADB_SERVICE_NAME, MARIADB_SERVICE_PORT),
                DOCKER_COMPOSE_CONTAINER.getServicePort(MARIADB_SERVICE_NAME, MARIADB_SERVICE_PORT));
        String username = "root";
        String password = "1234";
        log.info("MARIA_DB_CONTAINER url ==> {}", jdbcUrl);
        log.info("MARIA_DB_CONTAINER username ==> {}", username);
        log.info("MARIA_DB_CONTAINER password ==> {}", password);

        // Redis
        String redisHost = DOCKER_COMPOSE_CONTAINER.getServiceHost(REDIS_SERVICE_NAME, REDIS_SERVICE_PORT);
        Integer redisPort = DOCKER_COMPOSE_CONTAINER.getServicePort(REDIS_SERVICE_NAME, REDIS_SERVICE_PORT);
        String redisUrl = String.format("redis://:%s@%s:%d", "1234", redisHost, redisPort);
        log.info("REDIS_CONTAINER url ==> {}", redisUrl);

        TestPropertyValues.of(
                "spring.datasource.url=" + jdbcUrl,
                "spring.datasource.username=" + username,
                "spring.datasource.password=" + password,
                "spring.redis.url=" + redisUrl
        ).applyTo(applicationContext.getEnvironment());
    }
}
