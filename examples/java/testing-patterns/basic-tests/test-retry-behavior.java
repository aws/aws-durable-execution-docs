import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class TestRetryBehaviorTest {
    @Test
    void testStepRetriesAndSucceeds() {
        var runner = LocalDurableTestRunner.create(Object.class, new UnreliableOperationExample());

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

        var stepOp = runner.getOperation("unreliable_operation");
        assertNotNull(stepOp);
        assertEquals(OperationStatus.SUCCEEDED, stepOp.getStatus());
    }
}
