import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.retry.RetryStrategies;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class TestRetriesTest {

    @Test
    void retriesAndEventuallySucceeds() {
        var attempts = new AtomicInteger(0);

        var config = StepConfig.builder()
            .retryStrategy(RetryStrategies.exponentialBackoff(3))
            .build();

        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.step("flaky", String.class, ctx -> {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("transient error");
                }
                return "done";
            }, config)
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("done", result.getResult(String.class));
    }
}
