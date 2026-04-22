import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.model.OperationType;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class ChildContextTest {

    @Test
    void executesStepsInsideChildContext() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.runInChildContext("process", String.class, child -> {
                var a = child.step("step-a", String.class, ctx -> "result-a");
                var b = child.step("step-b", String.class, ctx -> "result-b");
                return a + ":" + b;
            })
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("result-a:result-b", result.getResult(String.class));

        var ctx = result.getOperation("process");
        assertNotNull(ctx);
        assertEquals(OperationType.CONTEXT, ctx.getType());
    }
}
