import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

@Test
void testMultipleWaits() {
    var runner = LocalDurableTestRunner.create(Object.class, new MultipleWaitExample());

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Find all wait operations
    var waitOps = result.getOperations().stream()
        .filter(op -> op.getType() == OperationType.WAIT)
        .toList();
    assertEquals(2, waitOps.size());

    // Verify both waits have names
    var waitNames = waitOps.stream().map(op -> op.getName()).toList();
    assertTrue(waitNames.contains("wait-1"));
    assertTrue(waitNames.contains("wait-2"));
}
