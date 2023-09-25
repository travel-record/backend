package world.trecord.infra.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

@Slf4j
public abstract class AbstractContainerBaseTest {

    static final DockerComposeContainer DOCKER_COMPOSE_CONTAINER;
    static final String MARIADB_SERVICE_NAME = "mariadb_1";
    static final String REDIS_SERVICE_NAME = "redis_1";
    static final int MARIADB_SERVICE_PORT = 3306;
    static final int REDIS_SERVICE_PORT = 6379;

    static {
        DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer(new File("docker-compose-test.yml"))
                .withExposedService(MARIADB_SERVICE_NAME, MARIADB_SERVICE_PORT, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                .withExposedService(REDIS_SERVICE_NAME, REDIS_SERVICE_PORT, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));
        DOCKER_COMPOSE_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = String.format("jdbc:mariadb://%s:%s/test",
                DOCKER_COMPOSE_CONTAINER.getServiceHost(MARIADB_SERVICE_NAME, MARIADB_SERVICE_PORT),
                DOCKER_COMPOSE_CONTAINER.getServicePort(MARIADB_SERVICE_NAME, MARIADB_SERVICE_PORT));
        String jdbcUsername = "root";
        String jdbcPassword = "1234";

        // Redis
        Integer redisPort = DOCKER_COMPOSE_CONTAINER.getServicePort(REDIS_SERVICE_NAME, REDIS_SERVICE_PORT);
        String redisPassword = "1234";
        String redisUrl = String.format("redis://:%s@%s:%d", redisPassword, redisPassword, redisPort);

        log.info("MARIA_DB_CONTAINER url ==> {}", jdbcUrl);
        log.info("MARIA_DB_CONTAINER jdbcUsername ==> {}", jdbcUsername);
        log.info("MARIA_DB_CONTAINER jdbcPassword ==> {}", jdbcPassword);
        log.info("REDIS_CONTAINER url ==> {}", redisUrl);

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> jdbcUsername);
        registry.add("spring.datasource.password", () -> jdbcPassword);
        registry.add("spring.redis.url", () -> redisUrl);
    }
}
