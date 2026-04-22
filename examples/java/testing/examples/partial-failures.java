import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class PartialFailuresTest {

    @Test
    void recordsWhichStepsSucceededBeforeFailure() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> {
                context.step("step-1", String.class, ctx -> "ok");
                context.step("step-2", String.class, ctx -> "ok");
                context.step("step-3", String.class, ctx -> {
                    throw new RuntimeException("step-3 failed");
                });
                return null;
            }
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.FAILED, result.getStatus());

        assertEquals(OperationStatus.SUCCEEDED, result.getOperation("step-1").getStatus());
        assertEquals(OperationStatus.SUCCEEDED, result.getOperation("step-2").getStatus());
        assertTrue(result.getError().isPresent());
    }
}
