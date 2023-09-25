package world.trecord.infra.test;

import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractConcurrencyTest extends AbstractIntegrationTest {

    protected final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    protected ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_CORES);
    }
}
