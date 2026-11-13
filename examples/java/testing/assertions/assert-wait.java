import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class AssertWaitTest {

    @Test
    void assertsOnWaitOperation() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> {
                context.wait("my-wait", Duration.ofSeconds(30));
                return "done";
            }
        );

        // The first run stops at the wait, so the wait is still pending.
        var pending = runner.run(null);

        assertEquals(ExecutionStatus.PENDING, pending.getStatus());

        var wait = pending.getOperation("my-wait");
        assertNotNull(wait);
        assertEquals(OperationType.WAIT, wait.getType());
        assertEquals(OperationStatus.STARTED, wait.getStatus());
        assertNotNull(wait.getWaitDetails().scheduledEndTimestamp());

        // Run to the end, and the wait succeeds.
        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals(OperationStatus.SUCCEEDED, result.getOperation("my-wait").getStatus());
    }
}
