import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class AssertStepTest {

    @Test
    void assertsOnStepOperation() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.step("compute", Integer.class, ctx -> 42)
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        var step = result.getOperation("compute");
        assertNotNull(step);
        assertEquals(OperationType.STEP, step.getType());
        assertEquals(OperationStatus.SUCCEEDED, step.getStatus());
        assertEquals(42, step.getStepResult(Integer.class));
    }
}
