import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class AssertChildContextTest {

    @Test
    void assertsOnChildContextAndItsOperations() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.runInChildContext("process", Integer.class,
                child -> child.step("compute", Integer.class, ctx -> 42))
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals(42, result.getResult(Integer.class));

        var ctx = result.getOperation("process");
        assertNotNull(ctx);
        assertEquals(OperationType.CONTEXT, ctx.getType());
        assertNotNull(ctx.getContextDetails());
    }
}
