import static org.junit.jupiter.api.Assertions.*;

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
        var config = StepConfig.builder()
            .retryStrategy(RetryStrategies.exponentialBackoff(3))
            .build();

        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.step("flaky", String.class, ctx -> {
                if (callCount.incrementAndGet() < 3) {
                    throw new RuntimeException("not yet");
                }
                return "ok";
            }, config)
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        var failedOps = result.getFailedOperations();
        assertEquals(2, failedOps.size());

        var succeededOps = result.getSucceededOperations();
        assertEquals(1, succeededOps.size());
    }
}
