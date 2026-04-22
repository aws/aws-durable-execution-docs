import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class LongWaitsTest {

    @Test
    void completesWithLongWait() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> {
                context.wait("cooling-off", Duration.ofHours(24));
                return context.step("after-wait", String.class, ctx -> "done");
            }
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("done", result.getResult(String.class));

        var waitOps = result.getOperations().stream()
            .filter(op -> op.getType() == OperationType.WAIT)
            .toList();
        assertEquals(1, waitOps.size());
        assertEquals("cooling-off", waitOps.get(0).getName());
    }
}
