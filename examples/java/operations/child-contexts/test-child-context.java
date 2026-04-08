import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class TestChildContext {

    @Test
    void childContextSucceeds() {
        var handler = new BasicChildContext();
        var runner = LocalDurableTestRunner.create(String.class, handler);

        var result = runner.runUntilComplete("order-1");

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("order-1:validated:charged", result.getResult(String.class));
    }

    @Test
    void recordsContextOperation() {
        var handler = new BasicChildContext();
        var runner = LocalDurableTestRunner.create(String.class, handler);

        var result = runner.runUntilComplete("order-1");

        var contextOps = result.getOperations().stream()
            .filter(op -> op.getType() == OperationType.CONTEXT)
            .toList();
        assertFalse(contextOps.isEmpty());
    }
}
