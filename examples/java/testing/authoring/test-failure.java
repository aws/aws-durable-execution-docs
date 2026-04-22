import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.model.ExecutionStatus;
import software.amazon.lambda.durable.testing.LocalDurableTestRunner;

class TestFailureTest {

    record Input(boolean fail) {}

    @Test
    void reportsFailedExecution() {
        var runner = LocalDurableTestRunner.create(
            Input.class,
            (input, context) -> {
                if (input.fail()) {
                    throw new RuntimeException("intentional failure");
                }
                return "ok";
            }
        );

        var result = runner.runUntilComplete(new Input(true));

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getError().isPresent());
    }
}
