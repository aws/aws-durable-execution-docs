import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.retry.RetryStrategies;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class FilterByStatusTest {

    @Test
    void filtersOperationsByStatus() {
        var callCount = new AtomicInteger(0);
        var retryConfig = StepConfig.builder()
            .retryStrategy(RetryStrategies.fixedDelay(3, Duration.ofSeconds(1)))
            .build();
        var noRetryConfig = StepConfig.builder()
            .retryStrategy(RetryStrategies.Presets.NO_RETRY)
            .build();

        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> {
                context.step("flaky", String.class, ctx -> {
                    if (callCount.incrementAndGet() < 3) {
                        throw new RuntimeException("not yet");
                    }
                    return "ok";
                }, retryConfig);
                try {
                    context.step("broken", String.class, ctx -> {
                        throw new IllegalStateException("always fails");
                    }, noRetryConfig);
                } catch (IllegalStateException e) {
                    // The SDK rethrows the step's original exception type.
                }
                return "done";
            }
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        // Each step is one operation, and its status is the final outcome.
        var failedOps = result.getFailedOperations();
        assertEquals(1, failedOps.size());
        assertEquals("broken", failedOps.get(0).getName());

        var succeededOps = result.getSucceededOperations();
        assertEquals(1, succeededOps.size());
        assertEquals("flaky", succeededOps.get(0).getName());
        assertEquals(OperationStatus.SUCCEEDED, succeededOps.get(0).getStatus());
    }
}
