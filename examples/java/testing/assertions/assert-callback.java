import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationStatus;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class AssertCallbackTest {

    @Test
    void completesCallbackFromTest() {
        var runner = LocalDurableTestRunner.create(
            String.class,
            (input, context) -> {
                var cb = context.createCallback("approval", String.class);
                return cb.get();
            }
        );

        // First invocation: creates the callback and suspends
        var pending = runner.run("test");
        assertEquals(ExecutionStatus.PENDING, pending.getStatus());

        var op = runner.getOperation("approval");
        assertNotNull(op);
        assertEquals(OperationType.CALLBACK, op.getType());
        assertEquals(OperationStatus.STARTED, op.getStatus());

        // Complete the callback from the test
        var callbackId = runner.getCallbackId("approval");
        runner.completeCallback(callbackId, "\"approved\"");

        // Second invocation: callback is complete, execution finishes
        var result = runner.run("test");
        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("approved", result.getResult(String.class));
    }
}
