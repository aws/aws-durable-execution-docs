import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.model.OperationType;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

@Test
void testWait() {
    var runner = LocalDurableTestRunner.create(Object.class, new BasicWaitExample());

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("Wait completed", result.getResult(String.class));

    // Find the wait operation
    var waitOps = result.getOperations().stream()
        .filter(op -> op.getType() == OperationType.WAIT)
        .toList();
    assertEquals(1, waitOps.size());
    assertNotNull(waitOps.get(0).getWaitDetails().scheduledEndTimestamp());
}
