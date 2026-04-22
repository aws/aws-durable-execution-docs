import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class MinimalTest {

    @Test
    void returnsExpectedResult() {
        var runner = LocalDurableTestRunner.create(
            Void.class,
            (input, context) -> context.step("greet", String.class, ctx -> "hello")
        );

        var result = runner.runUntilComplete(null);

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("hello", result.getResult(String.class));
    }
}
