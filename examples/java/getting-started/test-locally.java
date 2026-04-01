import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocally {

    @Test
    void testMyFunction() {
        var runner = LocalDurableTestRunner.create(
                Map.class,
                (Map<String, String> input, DurableContext context) ->
                        context.step("my-step", String.class,
                                stepCtx -> "processed-" + input.get("data")));

        var result = runner.runUntilComplete(Map.of("data", "test"));

        assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
        assertEquals("processed-test", result.getResult());
    }
}
