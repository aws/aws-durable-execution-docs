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
                context.wait(Duration.ofSeconds(30), "my-wait");
                return "done";
            }
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        var wait = result.getOperation("my-wait");
        assertNotNull(wait);
        assertEquals(OperationType.WAIT, wait.getType());
        assertEquals(OperationStatus.SUCCEEDED, wait.getStatus());
        assertNotNull(wait.getWaitDetails().scheduledEndTimestamp());
    }
}
